package model;

import dao.JDBCConnect;
import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AccountModel {

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

        try (Connection conn = JDBCConnect.getJDBCConnection();
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
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    accountList.add(account);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accountList;
    }

    public boolean isNameValid(String fullName, String role) {
        String query = "SELECT COUNT(*) FROM employees e " +
                "LEFT JOIN accounts a ON e.id = a.id_person " +
                "JOIN role r ON e.id_role = r.id " +  // Thêm bảng role để kiểm tra tên role
                "WHERE CONCAT(e.first_name, ' ', e.last_name) = ? " +  // Ghép first_name và last_name để so sánh với tên đầy đủ
                "AND r.role = ? AND a.id IS NULL";  // Kiểm tra role bằng chuỗi ký tự
        try (Connection conn = JDBCConnect.getJDBCConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fullName);  // Truyền tên đầy đủ vào câu truy vấn
            stmt.setString(2, role);  // Truyền tên role vào câu truy vấn
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 1;  // Trả về true nếu chưa có tài khoản
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public boolean isUsernameUnique(String username) {
        String query = "SELECT COUNT(*) FROM accounts WHERE username = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;  // Trả về true nếu username là duy nhất
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    // Phương thức thêm tài khoản mới
    public void addAccount(String username, String password, String name) {
        String insertSQL = "INSERT INTO accounts (username, password, id_person) " +
                "VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?))";

        try (Connection conn = JDBCConnect.getJDBCConnection();
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
        String query = "UPDATE accounts SET id_person = (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?), " +
                "username = ?, password = ? WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getUsername());
            pstmt.setString(3, account.getPassword());
            pstmt.setInt(4, account.getId());

            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức xóa tài khoản
    public boolean deleteAccount(int id) {
        String deleteSQL = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
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
