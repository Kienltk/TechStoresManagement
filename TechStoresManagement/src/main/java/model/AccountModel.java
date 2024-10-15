package model;

import dao.JDBCConnect;
import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AccountModel {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/tech_store_manager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    // Phương thức tải tất cả tài khoản (hoặc tìm kiếm nếu có query)
    public ObservableList<Account> loadAccounts(String searchQuery) {
        ObservableList<Account> accountList = FXCollections.observableArrayList();
        String searchSQL = "SELECT accounts.id AS account_id, " +
                "CONCAT(employees.first_name, ' ', employees.last_name) AS name, " +
                "role.role AS role, " +
                "accounts.username AS username, " +
                "accounts.password AS password " +
                "FROM accounts " +
                "INNER JOIN employees ON accounts.id_person = employees.id " +
                "INNER JOIN role ON employees.id_role = role.id " +
                "WHERE LOWER(accounts.username) LIKE LOWER(?) " +
                "OR LOWER(CONCAT(employees.first_name, ' ', employees.last_name)) LIKE LOWER(?) " +
                "OR LOWER(accounts.password) LIKE LOWER(?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String wildcardQuery = "%" + searchQuery + "%";
            pstmt.setString(1, wildcardQuery);
            pstmt.setString(2, wildcardQuery);
            pstmt.setString(3, wildcardQuery);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(
                            rs.getInt("account_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                    accountList.add(account);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accountList;
    }

    // Phương thức thêm tài khoản mới
    public void addAccount(String username, String password, String name) {
        String insertSQL = "INSERT INTO accounts (username, password, id_person) " +
                "VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?))";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Phương thức chỉnh sửa tài khoản
    public boolean updateAccount(Account account) {
        String query = "UPDATE accounts SET name = ?, username = ?, password = ? " +
                "WHERE id = ? AND employee_role = ? AND NOT EXISTS (" +
                "SELECT 1 FROM accounts WHERE username = ? AND id != ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters for the query
            stmt.setString(1, account.getName());
            stmt.setString(2, account.getUsername());
            stmt.setString(3, account.getPassword());
            stmt.setInt(4, account.getId());
            stmt.setString(5, account.getRole());  // Assuming the account object has the role
            stmt.setString(6, account.getUsername());
            stmt.setInt(7, account.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;  // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức xóa tài khoản
    public boolean deleteAccount(int id) {
        String deleteSQL = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
