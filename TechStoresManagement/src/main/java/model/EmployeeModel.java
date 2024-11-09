package model;

import dao.JDBCConnect;
import entity.Employee;
import javafx.util.Pair;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeModel {

    // Retrieve all employees
    private final Connection conn;

    public EmployeeModel() {
        this.conn = JDBCConnect.getJDBCConnection();
    }

    public List<Employee> getAllEmployees() {
        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.dob, e.email, e.phone_number, e.address, " +
                "e.salary, r.role, IFNULL(IFNULL(s.name, w.name), 'Business') AS workplace, e.id_role, e.id_store, e.id_warehouse " +
                "FROM employees e " +
                "LEFT JOIN role r ON e.id_role = r.id " +
                "LEFT JOIN stores s ON e.id_store = s.id " +
                "LEFT JOIN warehouses w ON e.id_warehouse = w.id";

        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getBoolean("gender"),
                        rs.getDate("dob"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getDouble("salary"),
                        rs.getInt("id_role"),
                        rs.getInt("id_store"),
                        rs.getInt("id_warehouse"),
                        rs.getString("role"),
                        rs.getString("workplace")
                );
                employees.add(employee);
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return employees;
    }

    public List<Employee> getAllEmployeesByWarehouse(int idWarehouse) {
        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.dob, e.email, e.phone_number, e.address, " +
                "       e.salary, r.role, w.name AS workplace, e.id_role, e.id_warehouse, e.id_store " +
                "FROM employees e " +
                "LEFT JOIN role r ON e.id_role = r.id " +
                "LEFT JOIN warehouses w ON e.id_warehouse = w.id " +
                "WHERE e.id_warehouse = ?";

        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idWarehouse);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = new Employee(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getBoolean("gender"),
                            rs.getDate("dob"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getDouble("salary"),
                            rs.getInt("id_role"),
                            rs.getObject("id_store") != null ? rs.getInt("id_store") : null,
                            rs.getObject("id_warehouse") != null ? rs.getInt("id_warehouse") : null,
                            rs.getString("role"),
                            rs.getString("workplace")
                    );
                    employees.add(employee);
                }
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return employees;
    }

    public List<Employee> getAllEmployeesByStore(int idStore) {
        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.dob, e.email, e.phone_number, e.address, " +
                "e.salary, r.role, s.name AS workplace, e.id_role, e.id_store, e.id_warehouse " +
                "FROM employees e " +
                "LEFT JOIN role r ON e.id_role = r.id " +
                "LEFT JOIN stores s ON e.id_store = s.id " +
                "WHERE e.id_store = ?";

        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = new Employee(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getBoolean("gender"),
                            rs.getDate("dob"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getDouble("salary"),
                            rs.getInt("id_role"),
                            rs.getObject("id_store") != null ? rs.getInt("id_store") : null,
                            rs.getObject("id_warehouse") != null ? rs.getInt("id_warehouse") : null,
                            rs.getString("role"),
                            rs.getString("workplace")
                    );
                    employees.add(employee);
                }
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return employees;
    }



    public List<Employee> searchEmployeesByName(String name) throws SQLException {
        String query = "SELECT e.*, "
                + "COALESCE(s.name, w.name, 'Business') AS workplace, "
                + "r.role AS role_name " // Thêm tên vai trò vào SELECT
                + "FROM employees e "
                + "LEFT JOIN stores s ON e.id_store = s.id "
                + "LEFT JOIN warehouses w ON e.id_warehouse = w.id "
                + "LEFT JOIN role r ON e.id_role = r.id " // Thêm phép nối với bảng role
                + "WHERE CONCAT(e.first_name, ' ', e.last_name) LIKE ?";

        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Employee employee = extractEmployeeFromResultSet(rs);
                employees.add(employee);
            }
        }
        return employees;
    }


    // Method để kiểm tra xem nhân viên có tài khoản hay không
    public boolean hasAccount(int employeeId) throws SQLException {
        String query = "SELECT COUNT(*) AS account_count FROM accounts WHERE id_person = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("account_count") > 0;
            }
        }
        return false;
    }

    public List<Pair<Integer, String>> getAllRolesWithIds() throws SQLException {
        String query = "SELECT id, role FROM role"; // Thêm id vào SELECT
        List<Pair<Integer, String>> roles = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                roles.add(new Pair<>(rs.getInt("id"), rs.getString("role"))); // Sử dụng Pair để lưu id và tên
            }
        }
        return roles;
    }


    public List<Pair<Integer, String>> getAllWarehousesWithIds() throws SQLException {
        String query = "SELECT id, name FROM warehouses";
        List<Pair<Integer, String>> warehouses = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                warehouses.add(new Pair<>(rs.getInt("id"), rs.getString("name")));
            }
        }
        return warehouses;
    }

    public List<Pair<Integer, String>> getAllStoresWithIds() throws SQLException {
        String query = "SELECT id, name FROM stores";
        List<Pair<Integer, String>> stores = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stores.add(new Pair<>(rs.getInt("id"), rs.getString("name")));
            }
        }
        return stores;
    }

    public String getStoreNameById(int storeId) {
        String storeName = null;
        String query = "SELECT name FROM stores WHERE id = ?"; // Thay đổi tên bảng nếu cần

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, storeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                storeName = rs.getString("name");
            }
        } catch (SQLException e) {
            System.out.println("Error getting store name: " + e.getMessage());
        }

        return storeName;
    }

    public String getWarehouseNameById(int warehouseId) {
        String warehouseName = null;
        String query = "SELECT name FROM warehouses WHERE id = ?"; // Thay đổi tên bảng nếu cần

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, warehouseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                warehouseName = rs.getString("name");
            }
        } catch (Exception e) {
            System.out.println("Error getting warehouse name: " + e.getMessage());
        }

        return warehouseName;
    }

    public void insertEmployee(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role, id_store, id_warehouse) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setBoolean(3, employee.isGender());
            stmt.setDate(4, employee.getDob());
            stmt.setString(5, employee.getEmail());
            stmt.setString(6, employee.getPhoneNumber());
            stmt.setString(7, employee.getAddress());
            stmt.setDouble(8, employee.getSalary());
            stmt.setInt(9, employee.getIdRole());

            // Chỉ gán idStore nếu không phải là 0, nếu không thì gán null
            if (employee.getIdStore() == 0) {
                stmt.setObject(10, null); // Gán null nếu idStore là 0
            } else {
                stmt.setInt(10, employee.getIdStore());
            }

            // Chỉ gán idWarehouse nếu không phải là 0, nếu không thì gán null
            if (employee.getIdWarehouse() == 0) {
                stmt.setObject(11, null); // Gán null nếu idWarehouse là 0
            } else {
                stmt.setInt(11, employee.getIdWarehouse());
            }

            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }


    public void updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, dob = ?, email = ?, phone_number = ?, " +
                "address = ?, salary = ?, id_role = ?, id_store = ?, id_warehouse = ? WHERE id = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setBoolean(3, employee.isGender());
            stmt.setDate(4, employee.getDob());
            stmt.setString(5, employee.getEmail());
            stmt.setString(6, employee.getPhoneNumber());
            stmt.setString(7, employee.getAddress());
            stmt.setDouble(8, employee.getSalary());
            stmt.setInt(9, employee.getIdRole());
            // Chỉ gán idStore nếu không phải là 0, nếu không thì gán null
            if (employee.getIdStore() == 0) {
                stmt.setObject(10, null); // Gán null nếu idStore là 0
            } else {
                stmt.setInt(10, employee.getIdStore());
            }

            // Chỉ gán idWarehouse nếu không phải là 0, nếu không thì gán null
            if (employee.getIdWarehouse() == 0) {
                stmt.setObject(11, null); // Gán null nếu idWarehouse là 0
            } else {
                stmt.setInt(11, employee.getIdWarehouse());
            }
            stmt.setInt(12, employee.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }


    public void deleteEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setGender(rs.getBoolean("gender"));
        employee.setDob(rs.getDate("dob"));
        employee.setEmail(rs.getString("email"));
        employee.setPhoneNumber(rs.getString("phone_number"));
        employee.setAddress(rs.getString("address"));
        employee.setSalary(rs.getDouble("salary"));

        // Lấy tên vai trò từ kết quả truy vấn
        employee.setRole(rs.getString("role_name")); // Sử dụng tên vai trò

        // Lấy workplace
        employee.setWorkplace(rs.getString("workplace"));

        // Kiểm tra hire_date có null không trước khi set
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate);
        } else {
            employee.setHireDate(null); // Hoặc một giá trị mặc định nếu bạn muốn
        }

        return employee;
    }

}
