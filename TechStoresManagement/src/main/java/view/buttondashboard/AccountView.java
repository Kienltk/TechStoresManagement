package view.buttondashboard;

import entity.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.AccountModel;
import validatepassword.Validate;

public class AccountView extends VBox {
    private ObservableList<Account> accountList;

    public AccountView() {
        // Title Label
        Label titleLabel = new Label("Account Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // New Account Button
        Button newAccountButton = new Button("New Account");
        newAccountButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");
        newAccountButton.setOnAction(event -> showNewAccountPopup());

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

//        TableColumn<Account, String> passwordColumn = new TableColumn<>("Password");
//        passwordColumn.setMinWidth(150);
//        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
//        TableColumn<Account, String> phoneColumn = new TableColumn<>("Phone Number");
//        phoneColumn.setMinWidth(150);
//        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());

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
        accountTable.getColumns().addAll(idColumn, nameColumn, usernameColumn, optionColumn);

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

        Label nameLabel = new Label("Full Name:");

        // Tạo ComboBox để hiển thị tên nhân viên
        ComboBox<String> nameComboBox = new ComboBox<>();
        AccountModel accountModel = new AccountModel();

        // Lấy danh sách tên nhân viên từ phương thức getAvailableEmployeeNames
        nameComboBox.setItems(FXCollections.observableArrayList(accountModel.getAvailableEmployeeNames()));

        // Nếu account đã tồn tại, đặt giá trị ComboBox thành tên của account
        nameComboBox.setValue(account.getName());

        // Label để hiển thị thông báo lỗi cho Full Name
        Label nameErrorLabel = new Label();
        nameErrorLabel.setTextFill(Color.RED); // Đặt màu chữ thành đỏ

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(account.getUsername());

        // Label để hiển thị thông báo lỗi cho Username
        Label usernameErrorLabel = new Label();
        usernameErrorLabel.setTextFill(Color.RED); // Đặt màu chữ thành đỏ

        Label oldPasswordLabel = new Label("Old Password:");
        PasswordField oldPasswordField = new PasswordField();

        // Label để hiển thị thông báo lỗi cho Old Password
        Label oldPasswordErrorLabel = new Label();
        oldPasswordErrorLabel.setTextFill(Color.RED);

        Label passwordLabel = new Label("New Password:");
        PasswordField passwordField = new PasswordField();

        // Label để hiển thị thông báo lỗi cho Password
        Label passwordErrorLabel = new Label();
        passwordErrorLabel.setTextFill(Color.RED);
        passwordField.setOnKeyReleased(event -> {
            String password = passwordField.getText();
            String validationMessage = Validate.validatePassword(password);
            if (!validationMessage.isEmpty()) {
                passwordErrorLabel.setText(validationMessage);
            } else {
                passwordErrorLabel.setText("");
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            String fullName = nameComboBox.getValue();
            String newUsername = usernameField.getText();
            String oldPassword = oldPasswordField.getText();
            String newPassword = passwordField.getText();
            String role = account.getRole();

            // Đặt lại các thông báo lỗi
            nameErrorLabel.setText("");
            usernameErrorLabel.setText("");
            oldPasswordErrorLabel.setText("");
            passwordErrorLabel.setText("");

            // Kiểm tra tên và username có hợp lệ không
            boolean hasError = false; // Biến để kiểm tra có lỗi hay không

            if (fullName == null) {
                nameErrorLabel.setText("Please select a full name.");
                hasError = true;
            } else if (!fullName.equals(account.getName()) && !accountModel.isNameValid(fullName, role)) {
                nameErrorLabel.setText("Name already exists or doesn't match the role.");
                hasError = true;
            }

            if (newUsername.isEmpty()) {
                usernameErrorLabel.setText("Username is required.");
                hasError = true;
            } else if (!newUsername.equals(account.getUsername()) && !accountModel.isUsernameUnique(newUsername)) {
                usernameErrorLabel.setText("Username already exists.");
                hasError = true;
            }

            // Kiểm tra mật khẩu cũ
            if (oldPassword.isEmpty()) {
                oldPasswordErrorLabel.setText("Old password is required.");
                hasError = true;
            } else if (!oldPassword.equals(account.getPassword())) {
                oldPasswordErrorLabel.setText("Old password is incorrect.");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Cập nhật thông tin tài khoản
            account.setName(fullName);
            account.setUsername(newUsername);
            account.setPassword(newPassword);

            boolean success = accountModel.updateAccount(account);
            if (success) {
                editStage.close();
                ((TableView<Account>) getChildren().get(3)).refresh();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Update Failed");
                errorAlert.showAndWait();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> editStage.close());

        VBox layout = new VBox(10, nameLabel, nameComboBox, nameErrorLabel, usernameLabel, usernameField, usernameErrorLabel,
                oldPasswordLabel, oldPasswordField, oldPasswordErrorLabel, passwordLabel, passwordField, passwordErrorLabel,
                new HBox(10, saveButton, cancelButton));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(layout, 300, 400);
        editStage.setScene(scene);
        editStage.showAndWait();
    }



    private void showNewAccountPopup() {
        Stage newAccountStage = new Stage();
        newAccountStage.initModality(Modality.APPLICATION_MODAL);
        newAccountStage.setTitle("New Account");

        // Tạo Label và ComboBox cho trường Full Name
        Label nameLabel = new Label("Full Name:");
        ComboBox<String> nameComboBox = new ComboBox<>();
        AccountModel accountModel = new AccountModel();
        nameComboBox.setItems(FXCollections.observableArrayList(accountModel.getAvailableEmployeeNames()));

        Label nameErrorLabel = new Label();
        nameErrorLabel.setTextFill(Color.RED);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label usernameErrorLabel = new Label();
        usernameErrorLabel.setTextFill(Color.RED);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label passwordErrorLabel = new Label();
        passwordErrorLabel.setTextFill(Color.RED);

        Label confirmPassword = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        Label confirmPasswordErrorLabel = new Label();
        confirmPasswordErrorLabel.setTextFill(Color.RED);

        usernameField.setOnKeyReleased(event -> {
            String username = usernameField.getText();
            if (username.isEmpty()) {
                usernameErrorLabel.setText("Username is required.");
            } else if (!accountModel.isUsernameUnique(username)) {
                usernameErrorLabel.setText("Username already exists. Please enter a unique username.");
            } else {
                usernameErrorLabel.setText("");
            }
        });

        passwordField.setOnKeyReleased(event -> {
            String password = passwordField.getText();
            String validationMessage = Validate.validatePassword(password);
            if (!validationMessage.isEmpty()) {
                passwordErrorLabel.setText(validationMessage);
            } else {
                passwordErrorLabel.setText("");
            }
        });

        // Nút Save
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            String fullName = nameComboBox.getValue();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPasswordValue = confirmPasswordField.getText();

            nameErrorLabel.setText("");
            usernameErrorLabel.setText("");
            passwordErrorLabel.setText("");
            confirmPasswordErrorLabel.setText("");

            boolean hasError = false;

            if (fullName == null) {
                nameErrorLabel.setText("Please select a full name.");
                hasError = true;
            }
            if (username.isEmpty()) {
                usernameErrorLabel.setText("Username is required.");
                hasError = true;
            }
            if (password.isEmpty()) {
                passwordErrorLabel.setText("Password is required.");
                hasError = true;
            }
            if (confirmPasswordValue.isEmpty()) {
                confirmPasswordErrorLabel.setText("Please confirm your password.");
                hasError = true;
            } else if (!password.equals(confirmPasswordValue)) {
                confirmPasswordErrorLabel.setText("Passwords do not match.");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            Account newAccount = new Account(0, fullName, username, password);

            boolean success = accountModel.addAccount(newAccount);

            if (success) {
                accountList.setAll(accountModel.loadAccounts(""));
                newAccountStage.close();
                ((TableView<Account>) getChildren().get(3)).refresh();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to add account.");
                errorAlert.setContentText("There was an error while adding the account.");
                errorAlert.showAndWait();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> newAccountStage.close());

        VBox layout = new VBox(10, nameLabel, nameComboBox, nameErrorLabel, usernameLabel, usernameField, usernameErrorLabel,
                passwordLabel, passwordField, passwordErrorLabel, confirmPassword, confirmPasswordField, confirmPasswordErrorLabel,
                new HBox(10, saveButton, cancelButton));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(layout, 300, 400);
        newAccountStage.setScene(scene);
        newAccountStage.showAndWait();
    }


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
