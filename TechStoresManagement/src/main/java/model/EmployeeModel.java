package model;

import dao.JDBCConnect;
import entity.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeModel {

    // Retrieve all employees
    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String query = "SELECT e.*, r.role, " +
                "IF(e.id_store IS NULL, w.name, s.name) AS workplace " +
                "FROM employees e " +
                "JOIN role r ON e.id_role = r.id " +
                "LEFT JOIN stores s ON e.id_store = s.id " +
                "LEFT JOIN warehouses w ON e.id_warehouse = w.id";

        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Employee employee = mapRowToEmployee(resultSet);
                employees.add(employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching employees", e);
        }
        return employees;
    }
    public void addEmployee(Employee employee, int roleId, int workplaceId) {
        String query = "INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role, id_store, id_warehouse, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setBoolean(3, employee.isGender());
            statement.setDate(4, employee.getDob());
            statement.setString(5, employee.getEmail());
            statement.setString(6, employee.getPhoneNumber());
            statement.setString(7, employee.getAddress());
            statement.setDate(8, employee.getHireDate());
            statement.setDouble(9, employee.getSalary());
            statement.setInt(10, roleId);

            if (workplaceId > 0) {
                statement.setInt(11, workplaceId);
                statement.setNull(12, Types.INTEGER);
            } else {
                statement.setNull(11, Types.INTEGER);
                statement.setInt(12, workplaceId);
            }

            statement.setString(13, employee.getStatus());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding employee", e);
        }
    }

    // Delete employee and associated accounts
    public void deleteEmployee(int employeeId) {
        executeUpdate("DELETE FROM accounts WHERE id_person = ?", employeeId);
        executeUpdate("DELETE FROM employees WHERE id = ?", employeeId);
    }

    public void updateEmployee(Employee employee) {
        String query = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, dob = ?, email = ?, " +
                "phone_number = ?, address = ?, hire_date = ?, salary = ?, id_role = ?, " +
                "id_store = ?, id_warehouse = ?, status = ? WHERE id = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setBoolean(3, employee.isGender());
            statement.setDate(4, employee.getDob());
            statement.setString(5, employee.getEmail());
            statement.setString(6, employee.getPhoneNumber());
            statement.setString(7, employee.getAddress());
            statement.setDate(8, employee.getHireDate());
            statement.setDouble(9, employee.getSalary());
            statement.setInt(10, employee.getIdRole());

            if (employee.getWorkplace().contains("Store")) {
                int idStore = getIdStoreFromName(employee.getWorkplace().replace("Store", ""));
                System.out.println(idStore);
                statement.setInt(11, idStore);
                statement.setNull(12, Types.INTEGER);
            } else if (employee.getWorkplace().contains("Warehouse")) {
                int idWarehouse = getIdWarehouseFromName(employee.getWorkplace().replace("Warehouse", ""));
                System.out.println(idWarehouse);
                statement.setNull(11, Types.INTEGER);
                statement.setInt(12, idWarehouse);
            } else {
                statement.setNull(11, Types.INTEGER);
                statement.setNull(12, Types.INTEGER);
            }

            statement.setString(13, employee.getStatus());
            statement.setInt(14, employee.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee", e);
        }
    }

    // Get all stores
    public List<String> getAllStores() {
        return fetchWorkplaces("SELECT name FROM stores");
    }

    // Get all warehouses
    public List<String> getAllWarehouses() {
        return fetchWorkplaces("SELECT name FROM warehouses");
    }
    public List<String> getAllRoles() {
        return fetchRoles("SELECT role FROM role");
    }

    private List<String> fetchRoles(String query) {
        List<String> roles = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                roles.add(resultSet.getString("role"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching roles", e);
        }
        return roles;
    }
    public int getRoleIdFromName(String roleName) {
        String query = "SELECT id FROM role WHERE role = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, roleName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    return -1; // hoặc throw một exception nếu không tìm thấy vai trò
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching role id", e);
        }
    }

    // Helper methods
    private List<String> fetchWorkplaces(String query) {
        List<String> workplaces = new ArrayList<>();
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workplaces.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workplaces", e);
        }
        return workplaces;
    }

    private void executeUpdate(String query, int parameter) {
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, parameter);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update", e);
        }
    }
    public int getIdStoreFromName(String storeName) {
        String query = "SELECT id FROM stores WHERE name = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, storeName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    return -1; // hoặc throw một exception nếu không tìm thấy store
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching store id", e);
        }
    }

    public int getIdWarehouseFromName(String warehouseName) {
        String query = "SELECT id FROM warehouses WHERE name = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, warehouseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    return -1; // hoặc throw một exception nếu không tìm thấy warehouse
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching warehouse id", e);
        }
    }

    private Employee mapRowToEmployee(ResultSet resultSet) throws SQLException {
        return new Employee(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getBoolean("gender"),
                resultSet.getDate("dob"),
                resultSet.getString("email"),
                resultSet.getString("phone_number"),
                resultSet.getString("address"),
                resultSet.getDate("hire_date"),
                resultSet.getDouble("salary"),
                resultSet.getInt("id_role"),
                resultSet.getObject("id_store") != null ? resultSet.getInt("id_store") : null,
                resultSet.getObject("id_warehouse") != null ? resultSet.getInt("id_warehouse") : null,
                resultSet.getString("status"),
                resultSet.getString("role"),
                resultSet.getString("workplace")
        );
    }

    public void addEmployee(Employee newEmployee) {
    }
}
