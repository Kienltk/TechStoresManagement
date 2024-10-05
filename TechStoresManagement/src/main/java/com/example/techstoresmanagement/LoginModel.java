package com.example.techstoresmanagement;



import dao.JDBCConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginModel {
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