package model;

import dao.JDBCConnect;
import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.PasswordUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountModel {

    public List<String> getAvailableEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        // Thực hiện câu truy vấn SQL để lấy tên nhân viên chưa có tài khoản
        String query = "SELECT CONCAT(first_name, ' ', last_name) AS full_name, id\n" +
                "FROM employees\n" +
                "WHERE id NOT IN (SELECT id_person FROM accounts)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                employeeNames.add(rs.getString("full_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeNames;
    }
    public ObservableList<Account> loadAccounts(String searchQuery) {
        ObservableList<Account> accountList = FXCollections.observableArrayList();
        String searchSQL = "SELECT accounts.id AS account_id, " +
                "CONCAT(employees.first_name, ' ', employees.last_name) AS name, " +
                "role.role AS role, " +
                "accounts.username AS username, " +
                "accounts.password AS password, " +
                "employees.email AS email " +  // Lấy thêm phone_number
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
                            rs.getString("role"),
                            rs.getString("email")
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

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 1;  // Trả về true nếu chưa có tài khoản
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
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



    public boolean addAccount(Account account) {
        // Truy vấn SQL để thêm tài khoản mới vào bảng accounts
        String insertSQL = "INSERT INTO accounts (username, password, id_person, email) " +
                "VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?), ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, account.getUsername());

            // Băm mật khẩu trước khi lưu
            String hashedPassword = encryptPassword(account.getPassword());
            pstmt.setString(2, hashedPassword);

            pstmt.setString(3, account.getName());
            pstmt.setString(4, account.getEmail()); // Thêm email vào truy vấn

            // Thực thi truy vấn
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu có ít nhất một dòng bị ảnh hưởng
        } catch (SQLException e) {
            e.printStackTrace(); // In ra lỗi nếu có
            return false; // Trả về false nếu có lỗi
        }
    }




    public boolean updateAccount(Account account) {
        // Truy vấn SQL để cập nhật thông tin tài khoản
        String query = "UPDATE accounts SET id_person = (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?), " +
                "username = ?, password = ?, email = ? WHERE id = ?"; // Thêm email vào truy vấn

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getUsername());

            // Băm mật khẩu trước khi lưu
            String hashedPassword = encryptPassword(account.getPassword());
            pstmt.setString(3, hashedPassword);

            pstmt.setString(4, account.getEmail()); // Thêm email vào truy vấn
            pstmt.setInt(5, account.getId());

            // Thực thi truy vấn
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Trả về true nếu có ít nhất một dòng bị ảnh hưởng

        } catch (SQLException e) {
            e.printStackTrace(); // In ra lỗi nếu có
            return false; // Trả về false nếu có lỗi
        }
    }

    public String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
