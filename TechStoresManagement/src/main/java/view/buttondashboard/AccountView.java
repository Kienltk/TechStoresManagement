package view.buttondashboard;

import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.AccountModel;

public class AccountView extends VBox {
    private ObservableList<Account> accountList;

    public AccountView() {
        // Title Label
        Label titleLabel = new Label("Account Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // New Account Button
        Button newAccountButton = new Button("New Account");
        newAccountButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search Account");

        searchField.setOnKeyReleased(event -> filterAccounts(searchField.getText()));

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setSpacing(10);

        // TableView for Account Data
        TableView<Account> accountTable = new TableView<>();

        // Table Columns
        TableColumn<Account, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(50);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<Account, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(230);
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Account, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setMinWidth(150);
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<Account, String> passwordColumn = new TableColumn<>("Password");
        passwordColumn.setMinWidth(150);
        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());

        TableColumn<Account, Void> optionColumn = new TableColumn<>("Option");
        optionColumn.setCellFactory(col -> new TableCell<>() {
            final Button editButton = new Button("Edit");
            final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: yellow;");
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox optionBox = new HBox(editButton, deleteButton);
                    optionBox.setSpacing(10);
                    setGraphic(optionBox);

                    editButton.setOnAction(event -> {
                        Account account = getTableView().getItems().get(getIndex());
                        System.out.println(account);
                        showEditPopup(account);
                    });

                    deleteButton.setOnAction(event -> {
                        Account account = getTableView().getItems().get(getIndex());
                        confirmDelete(account);
                    });
                }
            }
        });

        // Add Columns to Table
        accountTable.getColumns().addAll(idColumn, nameColumn, usernameColumn, passwordColumn, optionColumn);

        // ObservableList to hold Account data
        AccountModel accountModel = new AccountModel();
        accountList = FXCollections.observableArrayList();
        accountList.setAll(accountModel.loadAccounts("")); // Load tất cả tài khoản

        // Bind the data to the TableView
        accountTable.setItems(accountList);

        // Thêm các thành phần vào VBox
        this.getChildren().addAll(titleLabel, newAccountButton, searchBar, accountTable);
    }

    // Hàm lọc danh sách tài khoản dựa trên từ khóa tìm kiếm
    private void filterAccounts(String keyword) {
        ObservableList<Account> filteredList = FXCollections.observableArrayList();

        if (keyword.isEmpty()) {
            // Nếu ô tìm kiếm trống, hiển thị lại tất cả tài khoản
            ((TableView<Account>) getChildren().get(3)).setItems(accountList);
        } else {
            // Nếu có từ khóa, lọc danh sách tài khoản
            for (Account account : accountList) {
                if (account.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        account.getUsername().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(account);
                }
            }
            ((TableView<Account>) getChildren().get(3)).setItems(filteredList);
        }

        // Refresh table để đảm bảo các nút hiển thị đúng
        ((TableView<Account>) getChildren().get(3)).refresh();
    }

    // Hàm hiển thị pop-up chỉnh sửa tài khoản
    private void showEditPopup(Account account) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.setTitle("Edit Account");

        // Tạo Label và TextField cho tên đầy đủ
        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField(account.getName()); // Chứa tên hiện tại của tài khoản

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(account.getUsername());

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(account.getPassword());

        // Nút Save
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            String fullName = nameField.getText();  // Lấy tên đầy đủ từ TextField
            String newUsername = usernameField.getText();
            String newPassword = passwordField.getText();

            AccountModel accountModel = new AccountModel();
            String role = account.getRole();  // Lấy tên role từ đối tượng account

            // Kiểm tra điều kiện 1: Name cùng role và chưa có tài khoản
            boolean isNameValid = true; // Giả định là hợp lệ
            if (!fullName.equals(account.getName())) { // Chỉ kiểm tra nếu tên đã thay đổi
                isNameValid = accountModel.isNameValid(fullName, role);
            }

            // Kiểm tra điều kiện 2: Username không trùng
            boolean isUsernameUnique = true; // Giả định là duy nhất
            if (!newUsername.equals(account.getUsername())) { // Chỉ kiểm tra nếu username đã thay đổi
                isUsernameUnique = accountModel.isUsernameUnique(newUsername);
            }

            if (!isNameValid) {
                Alert nameAlert = new Alert(Alert.AlertType.ERROR);
                nameAlert.setTitle("Invalid Name");
                nameAlert.setHeaderText("Name already exists or doesn't match the role");
                nameAlert.setContentText("Please enter a valid name that matches the role and hasn't been used for another account.");
                nameAlert.showAndWait();
            } else if (!isUsernameUnique) {
                Alert usernameAlert = new Alert(Alert.AlertType.ERROR);
                usernameAlert.setTitle("Invalid Username");
                usernameAlert.setHeaderText("Username already exists");
                usernameAlert.setContentText("Please enter a unique username.");
                usernameAlert.showAndWait();
            } else {
                // Nếu tất cả điều kiện hợp lệ, cập nhật tài khoản
                account.setName(fullName);
                account.setUsername(newUsername);
                account.setPassword(newPassword);

                boolean success = accountModel.updateAccount(account);

                if (success) {
                    // Đóng cửa sổ sau khi lưu thành công
                    editStage.close();
                    ((TableView<Account>) getChildren().get(3)).refresh();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Update Failed");
                    errorAlert.setContentText("There was an error while updating the account.");
                    errorAlert.showAndWait();
                }
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> editStage.close());

        VBox layout = new VBox(10, nameLabel, nameField, usernameLabel, usernameField, passwordLabel, passwordField, new HBox(10, saveButton, cancelButton));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(layout, 300, 300);
        editStage.setScene(scene);
        editStage.showAndWait();
    }



    // Hàm xác nhận xóa tài khoản
    private void confirmDelete(Account account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to delete this account?");
        alert.setContentText("Account: " + account.getUsername());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Gọi phương thức xóa tài khoản trong AccountModel
                AccountModel accountModel = new AccountModel();
                boolean success = accountModel.deleteAccount(account.getId());

                if (success) {
                    accountList.remove(account);
                    ((TableView<Account>) getChildren().get(3)).refresh();
                } else {
                    // Hiển thị thông báo lỗi nếu không xóa được
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Delete Failed");
                    errorAlert.setContentText("There was an error while deleting the account.");
                    errorAlert.showAndWait();
                }
            }
        });
    }
}
