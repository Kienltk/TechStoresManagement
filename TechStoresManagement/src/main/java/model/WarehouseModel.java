package model;

import dao.JDBCConnect;
import entity.Product;
import entity.Warehouse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarehouseModel {

    // Lấy danh sách cửa hàng từ database
    public List<Warehouse> getWarehouses(String search) {
        List<Warehouse> warehouses = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT s.id, s.name, s.address, " +
                    "CONCAT(e.first_name, ' ', e.last_name) AS managerName " +
                    "FROM warehouses s " +
                    "LEFT JOIN employees e ON s.id = e.id_warehouse AND e.id_role = (SELECT id FROM role WHERE role = 'Warehouse Management') " +
                    "WHERE s.name LIKE ? OR s.address LIKE ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                warehouse.setManagerName(rs.getString("managerName"));

                // Tính tổng tồn kho cho cửa hàng
                int totalInventory = calculateTotalInventory(warehouse.getId());
                warehouse.setTotalInventory(totalInventory); // Đảm bảo bạn đã có setter cho totalInventory

                warehouses.add(warehouse);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouses;
    }


    private int calculateTotalInventory(int warehouseId) {
        int totalInventory = 0;
        String query = "SELECT SUM(quantity) AS total_quantity FROM products_warehouse WHERE id_warehouse = ?";

        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, warehouseId); // Đặt ID cửa hàng vào câu lệnh truy vấn
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                totalInventory = rs.getInt("total_quantity"); // Lấy tổng số lượng tồn kho
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalInventory;
    }

    // Thêm mới cửa hàng
    public void addWarehouse(String name, String address) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "INSERT INTO warehouses (name, address) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateWarehouse(int warehouseId, String name, String address, Integer newManagerId) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            // Bắt đầu transaction
            connection.setAutoCommit(false);

            // Cập nhật tên và địa chỉ của cửa hàng
            String warehouseUpdateQuery = "UPDATE warehouses SET name = ?, address = ? WHERE id = ?";
            try (PreparedStatement warehouseStmt = connection.prepareStatement(warehouseUpdateQuery)) {
                warehouseStmt.setString(1, name);
                warehouseStmt.setString(2, address);
                warehouseStmt.setInt(3, warehouseId);
                warehouseStmt.executeUpdate();
            }

            // Cập nhật người quản lý nếu managerId không null
            if (newManagerId != null) {
                // Lấy ID của người quản lý hiện tại
                String currentManagerQuery = "SELECT id FROM employees WHERE id_warehouse = ? AND id_role = (SELECT id FROM role WHERE role = 'Warehouse Management')";
                Integer currentManagerId = null;
                try (PreparedStatement currentManagerStmt = connection.prepareStatement(currentManagerQuery)) {
                    currentManagerStmt.setInt(1, warehouseId);
                    try (ResultSet rs = currentManagerStmt.executeQuery()) {
                        if (rs.next()) {
                            currentManagerId = rs.getInt("id");
                        }
                    }
                }

                // Nếu có người quản lý hiện tại và không phải là cùng một người với người quản lý mới
                if (currentManagerId != null && !currentManagerId.equals(newManagerId)) {
                    // Cập nhật vai trò của quản lý cũ thành "Employee"
                    String updateOldManagerRoleQuery = "UPDATE employees SET id_role = (SELECT id FROM role WHERE role = 'Employee') WHERE id = ?";
                    try (PreparedStatement updateOldManagerStmt = connection.prepareStatement(updateOldManagerRoleQuery)) {
                        updateOldManagerStmt.setInt(1, currentManagerId);
                        updateOldManagerStmt.executeUpdate();
                    }
                }

                // Cập nhật vai trò của người quản lý mới thành "Warehouse Management" và đặt id_warehouse cho họ
                String updateNewManagerQuery = "UPDATE employees SET id_warehouse = ?, id_role = (SELECT id FROM role WHERE role = 'Warehouse Management') WHERE id = ?";
                try (PreparedStatement updateNewManagerStmt = connection.prepareStatement(updateNewManagerQuery)) {
                    updateNewManagerStmt.setInt(1, warehouseId);
                    updateNewManagerStmt.setInt(2, newManagerId);
                    updateNewManagerStmt.executeUpdate();
                }
            }

            // Commit transaction
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Xóa cửa hàng
    public void deleteWarehouse(int id) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "DELETE FROM warehouses WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra cửa hàng có sản phẩm tồn kho hay không
    public boolean hasInventory(int warehouseId) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM products_warehouse WHERE id_warehouse = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, warehouseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra trùng lặp tên hoặc địa chỉ
    public boolean isWarehouseNameDuplicate(String name) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM warehouses WHERE name = ?"; // Chỉ kiểm tra tên
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name); // Truyền tên vào câu truy vấn
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Nếu có ít nhất một kết quả, tức là tên bị trùng
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Nếu không có kết quả nào, tên không bị trùng
    }

    public boolean isWarehouseAddressDuplicate(String address) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM warehouses WHERE address = ?"; // Chỉ kiểm tra địa chỉ
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, address); // Truyền địa chỉ vào câu truy vấn
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Nếu có ít nhất một kết quả, tức là địa chỉ bị trùng
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Nếu không có kết quả nào, địa chỉ không bị trùng
    }

    public List<Product> getProductsByWarehouseId(int warehouseId) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            // Cập nhật truy vấn SQL để chỉ lấy thông tin cần thiết
            String query = "SELECT p.id, p.product_name, p.brand, pw.quantity AS stock " +
                    "FROM products p " +
                    "JOIN products_warehouse pw ON p.id = pw.id_product " +
                    "WHERE pw.id_warehouse = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, warehouseId); // ID kho

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("product_name");
                String brand = rs.getString("brand");
                int stock = rs.getInt("stock");

                // Tạo đối tượng Product
                Product product = new Product(id, name, brand, stock);
                products.add(product);
                System.out.println(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    // Trong WarehouseModel.java
    public List<String> getAllEmployees() {
        List<String> employeeNames = new ArrayList<>();
        String sql = "SELECT first_name, last_name FROM employees";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                employeeNames.add(fullName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeNames;
    }

    public String getEmployeeRoleByName(String name) {
        String role = null;
        String sql = "SELECT role.role FROM employees INNER JOIN role ON employees.id_role = role.id WHERE CONCAT(first_name, ' ', last_name) = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }

    public Integer getEmployeeIdByName(String name) {
        Integer id = null;
        String sql = "SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

}
