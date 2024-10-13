package controller;

import entity.Account;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import java.sql.*;
import java.util.Optional;

public class AccountController {

    @FXML
    private TableView<Account> accountTable;
    @FXML
    private TableColumn<Account, Integer> accountIdColumn;
    @FXML
    private TableColumn<Account, String> nameColumn;
    @FXML
    private TableColumn<Account, String> roleColumn;
    @FXML
    private TableColumn<Account, String> usernameColumn;
    @FXML
    private TableColumn<Account, String> passwordColumn;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField roleField;
    @FXML
    private TextField searchField; // Ô tìm kiếm

    private ObservableList<Account> accountList = FXCollections.observableArrayList();

    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim();
        loadAccounts(searchQuery);
    }
    @FXML
    public void initialize() {
        accountIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));

        loadAccounts(""); // Gọi hàm loadAccounts với chuỗi rỗng

        // Thêm sự kiện cho ô tìm kiếm
        searchField.setOnKeyReleased(event -> handleSearch());
    }

    private void loadAccounts(String searchQuery) {
        accountList.clear();

        String searchSQL = "SELECT accounts.id AS account_id, " +
                "CONCAT(employees.first_name, ' ', employees.last_name) AS name, " +
                "role.role AS role, " +
                "accounts.username AS username, " +
                "accounts.password AS password " +
                "FROM accounts " +
                "INNER JOIN employees ON accounts.id_person = employees.id " +
                "INNER JOIN role ON employees.id_role = role.id " +
                "WHERE LOWER(accounts.username) LIKE LOWER(?) " +
                "OR LOWER(role.role) LIKE LOWER(?) " +
                "OR LOWER(CONCAT(employees.first_name, ' ', employees.last_name)) LIKE LOWER(?) " + // Tìm theo tên
                "OR LOWER(accounts.password) LIKE LOWER(?)"; // Tìm theo mật khẩu

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_store_manager", "root", "12345678");
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String wildcardQuery = "%" + searchQuery + "%"; // Thêm ký tự đại diện

            pstmt.setString(1, wildcardQuery);
            pstmt.setString(2, wildcardQuery);
            pstmt.setString(3, wildcardQuery);
            pstmt.setString(4, wildcardQuery);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(rs.getInt("account_id"), rs.getString("name"),
                            rs.getString("role"), rs.getString("username"),
                            rs.getString("password"));
                    accountList.add(account);
                }

                accountTable.setItems(accountList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddAccount() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String name = nameField.getText();
        String role = roleField.getText();

        String insertSQL = "INSERT INTO accounts (username, password, id_person) VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ? AND id_role = (SELECT id FROM role WHERE role = ?)))";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_store_manager", "root", "12345678");
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
            loadAccounts(""); // Cập nhật danh sách sau khi thêm tài khoản

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditAccount() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || role.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Information");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all fields.");
                alert.showAndWait();
                return;
            }

            String updateSQL = "UPDATE accounts SET username = ?, password = ?, id_person = (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ? AND id_role = (SELECT id FROM role WHERE role = ?)) WHERE id = ?";

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_store_manager", "root", "12345678");
                 PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, role);
                pstmt.setInt(5, selectedAccount.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    loadAccounts(""); // Cập nhật danh sách sau khi chỉnh sửa
                    System.out.println("Account ID: " + selectedAccount.getId() + " updated successfully.");
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Update Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("No account was updated. Please check the details.");
                    alert.showAndWait();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while updating the account.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Account Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an account to edit.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this account?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String deleteSQL = "DELETE FROM accounts WHERE id = ?";
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tech_store_manager", "root", "12345678");
                     PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

                    pstmt.setInt(1, selectedAccount.getId());
                    pstmt.executeUpdate();
                    loadAccounts(""); // Cập nhật danh sách sau khi xóa tài khoản

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Account Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an account to delete.");
            alert.showAndWait();
        }
    }
}
