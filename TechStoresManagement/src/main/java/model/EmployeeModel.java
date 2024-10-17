package model;

import dao.JDBCConnect;
import entity.Employee;


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
                "e.salary, r.role, IFNULL(s.name, w.name) AS workplace, e.id_role, e.id_store, e.id_warehouse " +
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
                        rs.getInt("id_role"),  // Lấy idRole từ result set
                        rs.getInt("id_store"), // Lấy idStore từ result set
                        rs.getInt("id_warehouse"), // Lấy idWarehouse từ result set
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

    public List<Employee> searchEmployeesByName(String name) throws SQLException {
        String query = "SELECT * FROM employees WHERE CONCAT(first_name, ' ', last_name) LIKE ?";
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
        String query = "SELECT COUNT(*) AS account_count FROM accounts WHERE employee_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("account_count") > 0;
            }
        }
        return false;
    }

    // Method để lấy tất cả chức vụ từ cơ sở dữ liệu
    public List<String> getAllRoles() throws SQLException {
        String query = "SELECT role FROM role";
        List<String> roles = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                roles.add(rs.getString("role"));
            }
        }
        return roles;
    }

    // Method để lấy tất cả địa điểm làm việc (cửa hàng và kho) từ cơ sở dữ liệu
    public List<String> getAllWorkplaces() throws SQLException {
        String warehouseQuery = "SELECT name FROM warehouses";
        String storeQuery = "SELECT name FROM stores";
        List<String> workplaces = new ArrayList<>();

        // Lấy tên từ bảng warehouses (kho)
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(warehouseQuery)) {
            while (rs.next()) {
                workplaces.add("Warehouse: " + rs.getString("name"));
            }
        }

        // Lấy tên từ bảng stores (cửa hàng)
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(storeQuery)) {
            while (rs.next()) {
                workplaces.add("Store: " + rs.getString("name"));
            }
        }

        return workplaces;
    }



    public void insertEmployee(Employee employee){
        String sql = "INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role, id_store, id_warehouse) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW, ?, ?, ?, ?)";
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
            stmt.setObject(10, employee.getIdStore());
            stmt.setObject(11, employee.getIdWarehouse());
            stmt.executeUpdate();
        }catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    public void updateEmployee(Employee employee){
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
            stmt.setObject(10, employee.getIdStore());
            stmt.setObject(11, employee.getIdWarehouse());
            stmt.setInt(12, employee.getId());
            stmt.executeUpdate();
        }catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    public void deleteEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        }catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setGender(rs.getBoolean("gender"));
        employee.setDob(rs.getDate("dob"));
        employee.setEmail(rs.getString("email"));
        employee.setPhoneNumber(rs.getString("phone_number"));
        employee.setAddress(rs.getString("address"));
        employee.setSalary(rs.getDouble("salary"));
        employee.setRole(rs.getString("role"));
        employee.setWorkplace(rs.getString("workplace"));
        return employee;
    }
}
