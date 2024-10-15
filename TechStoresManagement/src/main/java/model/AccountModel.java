package model;

import dao.JDBCConnect;
import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.PasswordUtil;

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


    public boolean isValidName(String firstName, String lastName) {
        String query = "SELECT COUNT(*) FROM employees e " +
                "LEFT JOIN accounts a ON e.id = a.id_person " +
                "WHERE e.first_name = ? " +  // Kiểm tra first_name
                "AND e.last_name = ? " +     // Kiểm tra last_name
                "AND a.id IS NULL";          // Kiểm tra rằng nhân viên chưa có tài khoản

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, firstName);  // Truyền firstName vào câu truy vấn
            stmt.setString(2, lastName);    // Truyền lastName vào câu truy vấn
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 1;  // Trả về true nếu chưa có tài khoản
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;  // Trả về false nếu không tìm thấy
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
    public boolean addAccount(Account account) {
        String insertSQL = "INSERT INTO accounts (username, password, id_person) " +
                "VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?))";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, account.getUsername());
            // Băm mật khẩu trước khi lưu
            String hashedPassword = PasswordUtil.hashPassword(account.getPassword());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, account.getName());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
            // Băm mật khẩu trước khi lưu
            String hashedPassword = PasswordUtil.hashPassword(account.getPassword());
            pstmt.setString(3, hashedPassword);
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
