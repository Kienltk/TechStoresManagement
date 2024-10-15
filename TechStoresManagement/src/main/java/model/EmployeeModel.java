package model;

import dao.JDBCConnect;
import entity.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // Phương thức kiểm tra xem nhân viên có tài khoản hay không
    public List<String> getAccountsByEmployeeId(int employeeId) {
        List<String> accounts = new ArrayList<>();
        String query = "SELECT username FROM accounts WHERE id_person = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    accounts.add(resultSet.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching accounts", e);
        }
        return accounts;
    }
    // Phương thức xóa nhân viên
    public void deleteEmployee(int employeeId) {
        List<String> accounts = getAccountsByEmployeeId(employeeId);
        if (!accounts.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete employee");
            alert.setHeaderText("This employee has an account");
            alert.setContentText("Account : " + accounts.get(0) + ". Do you want to delete both ?");
            ButtonType deleteButtonType = new ButtonType("Delete");
            ButtonType skipButtonType = new ButtonType("Skip");
            alert.getButtonTypes().setAll(deleteButtonType, skipButtonType);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == deleteButtonType) {
                executeUpdate("DELETE FROM accounts WHERE id_person = ?", employeeId);
                executeUpdate("DELETE FROM employees WHERE id = ?", employeeId);
            }
        } else {
            executeUpdate("DELETE FROM employees WHERE id = ?", employeeId);
        }
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
    public String getRoleName(int roleId) {
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT role FROM role WHERE id = ?")) {

            statement.setInt(1, roleId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("role");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching role name", e);
        }

        return null;
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
