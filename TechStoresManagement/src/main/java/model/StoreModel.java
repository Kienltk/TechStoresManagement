package model;

import dao.JDBCConnect;
import entity.Product;
import entity.Store;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreModel {

    // Lấy danh sách cửa hàng từ database
    public List<Store> getStores(String search) {
        List<Store> stores = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT s.id, s.name, s.address, " +
                    "CONCAT(e.first_name, ' ', e.last_name) AS managerName " +
                    "FROM stores s " +
                    "LEFT JOIN employees e ON s.id = e.id_store AND e.id_role = (SELECT id FROM role WHERE role = 'Store Management') " +
                    "WHERE s.name LIKE ? OR s.address LIKE ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Store store = new Store(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                store.setManagerName(rs.getString("managerName"));

                // Tính tổng tồn kho cho cửa hàng
                int totalInventory = calculateTotalInventory(store.getId());
                store.setTotalInventory(totalInventory); // Đảm bảo bạn đã có setter cho totalInventory

                stores.add(store);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stores;
    }

    private int calculateTotalInventory(int storeId) {
        int totalInventory = 0;
        String query = "SELECT SUM(quantity) AS total_quantity FROM products_store WHERE id_store = ?";

        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, storeId); // Đặt ID cửa hàng vào câu lệnh truy vấn
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
    public void addStore(String name, String address) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "INSERT INTO stores (name, address) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStore(int storeId, String name, String address, Integer newManagerId) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            // Bắt đầu transaction
            connection.setAutoCommit(false);

            // Cập nhật tên và địa chỉ của cửa hàng
            String storeUpdateQuery = "UPDATE stores SET name = ?, address = ? WHERE id = ?";
            try (PreparedStatement storeStmt = connection.prepareStatement(storeUpdateQuery)) {
                storeStmt.setString(1, name);
                storeStmt.setString(2, address);
                storeStmt.setInt(3, storeId);
                storeStmt.executeUpdate();
            }

            // Cập nhật người quản lý nếu managerId không null
            if (newManagerId != null) {
                // Lấy ID của người quản lý hiện tại
                String currentManagerQuery = "SELECT id FROM employees WHERE id_store = ? AND id_role = (SELECT id FROM role WHERE role = 'Store Management')";
                Integer currentManagerId = null;
                try (PreparedStatement currentManagerStmt = connection.prepareStatement(currentManagerQuery)) {
                    currentManagerStmt.setInt(1, storeId);
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

                // Cập nhật vai trò của người quản lý mới thành "Store Management" và đặt id_store cho họ
                String updateNewManagerQuery = "UPDATE employees SET id_store = ?, id_role = (SELECT id FROM role WHERE role = 'Store Management') WHERE id = ?";
                try (PreparedStatement updateNewManagerStmt = connection.prepareStatement(updateNewManagerQuery)) {
                    updateNewManagerStmt.setInt(1, storeId);
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
    public void deleteStore(int id) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "DELETE FROM stores WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tính toán doanh thu, lợi nhuận, và vốn
    public double[] calculateFinancials(int storeId) {
        double turnover = 0;
        double profit = 0;
        double capital = 0;

        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            // Truy vấn tổng doanh thu và lợi nhuận từ bảng receipts
            String query = "SELECT SUM(turnover), SUM(profit), SUM(capital) FROM store_financial WHERE id_store = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                turnover = rs.getDouble(1); // Tổng doanh thu
                profit = rs.getDouble(2);    // Tổng lợi nhuận
                capital = rs.getDouble(3);
            }

//            // Truy vấn tổng vốn từ bảng products_store
//            query = "SELECT SUM(ps.quantity * p.purchase_price) FROM products_store ps " +
//                    "JOIN products p ON ps.id_product = p.id " +
//                    "WHERE ps.id_store = ?";
//            stmt = connection.prepareStatement(query);
//            stmt.setInt(1, storeId);
//            rs = stmt.executeQuery();
//            if (rs.next()) {
//                capital = rs.getDouble(1);
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new double[]{turnover, profit, capital};
    }


    // Kiểm tra cửa hàng có sản phẩm tồn kho hay không
    public boolean hasInventory(int storeId) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM products_store WHERE id_store = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, storeId);
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
    public boolean isStoreNameDuplicate(String name) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM stores WHERE name = ?"; // Chỉ kiểm tra tên
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

    public boolean isStoreAddressDuplicate(String address) {
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            String query = "SELECT COUNT(*) FROM stores WHERE address = ?"; // Chỉ kiểm tra địa chỉ
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

    public List<Product> getProductsByStoreId(int storeId) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection()) {
            // Cập nhật truy vấn SQL
            String query = "SELECT p.id, p.product_name, p.brand, ps.quantity AS stock, " +
                    "IFNULL(SUM(pr.quantity), 0) AS sold_quantity, " +
                    "IFNULL(SUM(pr.profit), 0) AS profit " +
                    "FROM products p " +
                    "JOIN products_store ps ON p.id = ps.id_product " +
                    "LEFT JOIN products_receipt pr ON p.id = pr.id_product " +
                    "LEFT JOIN receipts r ON pr.id_receipt = r.id AND r.id_store = ? " +
                    "WHERE ps.id_store = ? " +
                    "GROUP BY p.id, ps.quantity";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, storeId); // ID cửa hàng cho receipts
            stmt.setInt(2, storeId); // ID cửa hàng cho products_store
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("product_name");
                String brand = rs.getString("brand");
                int stock = rs.getInt("stock");
                int soldQuantity = rs.getInt("sold_quantity");
                double profit = rs.getDouble("profit");

                // Tạo đối tượng Product
                Product product = new Product(id, name, brand, stock, soldQuantity, profit);
                products.add(product);
                System.out.println(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Trong StoreModel.java
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

