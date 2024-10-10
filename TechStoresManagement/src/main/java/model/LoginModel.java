package model;


import dao.JDBCConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginModel {
    public Map<String, Object> getRoleAndLocation(String username) {
        Map<String, Object> userInfo = new HashMap<>();
        String sql = "SELECT r.role, e.id_store, e.id_warehouse, e.first_name, e.last_name " +
                "FROM accounts a " +
                "JOIN employees e ON a.id_person = e.id " +
                "JOIN role r ON e.id_role = r.id " +
                "WHERE a.username = ?";

        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userInfo.put("role", resultSet.getString("role"));
                userInfo.put("id_store", resultSet.getInt("id_store"));
                userInfo.put("id_warehouse", resultSet.getInt("id_warehouse"));
                userInfo.put("employee_name", resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return userInfo;
    }



    public boolean validateLogin(String username, String password) {
        boolean isValid = false;
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";

        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                isValid = true;
            }

        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return isValid;
    }
}