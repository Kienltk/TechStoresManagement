package model;

import dao.JDBCConnect;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeModel {

    private Connection getConnection() throws SQLException {
        return JDBCConnect.getJDBCConnection();
    }

    private void executeUpdate(String query, Object... params) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
        }
    }

    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String query = "SELECT e.*, r.role, " +
                "IF(e.id_store IS NULL, w.name, s.name) AS workplace " +
                "FROM employees e " +
                "JOIN role r ON e.id_role = r.id " +
                "LEFT JOIN stores s ON e.id_store = s.id " +
                "LEFT JOIN warehouses w ON e.id_warehouse = w.id";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Employee employee = mapRowToEmployee(resultSet);
                employees.add(employee);
            }
        } catch (SQLException e) {
            logError("Error fetching employees", e);
        }
        return employees;
    }

    public void addEmployee(Employee employee, int roleId, int idStore, int idWarehouse) {
        String query = "INSERT INTO employees (first_name, last_name, gender, dob, email, phone_number, address, hire_date, salary, id_role, id_store, id_warehouse, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            executeUpdate(query,
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.isGender(),
                    employee.getDob(),
                    employee.getEmail(),
                    employee.getPhoneNumber(),
                    employee.getAddress(),
                    employee.getHireDate(),
                    employee.getSalary(),
                    roleId,
                    idStore,
                    idWarehouse,
                    employee.getStatus());
        } catch (SQLException e) {
            logError("Error adding employee", e);
        }
    }

    public List<String> getAccountsByEmployeeId(int employeeId) {
        List<String> accounts = new ArrayList<>();
        String query = "SELECT username FROM accounts WHERE id_person = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(resultSet.getString("username"));
                }
            }
        } catch (SQLException e) {
            logError("Error fetching accounts", e);
        }
        return accounts;
    }

    public void deleteEmployee(int employeeId) throws SQLException {
        List<String> accounts = getAccountsByEmployeeId(employeeId);
        if (!accounts.isEmpty()) {
            executeUpdate("DELETE FROM accounts WHERE id_person = ?", employeeId);
        }
        executeUpdate("DELETE FROM employees WHERE id = ?", employeeId);
    }

    public void updateEmployee(Employee employee,int roleId, int idStore, int idWarehouse) {
        String query = "UPDATE employees SET first_name = ?, last_name = ?, gender = ?, dob = ?, email = ?, " +
                "phone_number = ?, address = ?, hire_date = ?, salary = ?, id_role = ?, " +
                "id_store = ?, id_warehouse = ?, status = ? WHERE id = ?";
        try {
            executeUpdate(query,
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.isGender(),
                    employee.getDob(),
                    employee.getEmail(),
                    employee.getPhoneNumber(),
                    employee.getAddress(),
                    employee.getHireDate(),
                    employee.getSalary(),
                    roleId,
                    idStore,
                    idWarehouse,
                    employee.getStatus(),
                    employee.getId());
        } catch (SQLException e) {
            logError("Error updating employee", e);
        }
    }
    public ObservableList<Workplace> getAllWorkplaces() {
        ObservableList<Workplace> workplaces = FXCollections.observableArrayList();
        String query = "SELECT id, name FROM stores UNION SELECT id, name FROM warehouses";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                workplaces.add(new Workplace(rs.getInt("id"), rs.getString("name"), true));
            }
        } catch (SQLException e) {
            logError("Error fetching workplaces", e);
        }
        return workplaces;
    }
    public List<Role> getAllRoles() {
    List<Role> roles = new ArrayList<>();
    String query = "SELECT id, name FROM role";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        while (rs.next()) {
            roles.add(new Role(rs.getInt("id"), rs.getString("name")));
        }
    } catch (SQLException e) {
        logError("Error fetching roles", e);
    }
    return roles;
}
    public List<Store> getAllStores() {
        List<Store> stores = new ArrayList<>();
        String query = "SELECT id, name FROM stores";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                stores.add(new Store(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logError("Error fetching roles", e);
        }
        return stores;
    }
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();
        String query = "SELECT id, name FROM warehouses";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                warehouses.add(new Warehouse(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logError("Error fetching Warehouses", e);
        }
        return warehouses;
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

    private void logError(String message, SQLException e) {
        // Log the error using a logging framework, such as Log4j or Java Util Logging
        System.err.println(message + ": " + e.getMessage());
    }
}