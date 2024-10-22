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
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;
import view.stage.EditSuccess;

import java.util.Objects;

public class AccountView extends VBox {
    private ObservableList<Account> accountList;
    private ObservableList<Account> paginatedAccountList = FXCollections.observableArrayList();
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();

    public AccountView() {
        // Title Label
        Label titleLabel = new Label("Account Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // New Account Button
        Button newAccountButton = new Button("New Account");
        newAccountButton.getStyleClass().add("button-pagination");
        newAccountButton.setOnAction(event -> showNewAccountPopup());

        TextField searchField = new TextField();
        searchField.setPromptText("Search Account");
        searchField.setOnKeyReleased(event -> filterAccounts(searchField.getText()));

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 10 10 620;");
        
        searchField.getStyleClass().add("search-box");

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll( newAccountButton,searchBar);

        // TableView for Account Data
        TableView<Account> accountTable = new TableView<>();
        // Table Columns
        TableColumn<Account, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(85);
        idColumn.getStyleClass().add("column");
        idColumn.setStyle("-fx-alignment: CENTER;");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<Account, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(250);
        nameColumn.getStyleClass().add("column");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Account, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setMinWidth(270);
        usernameColumn.getStyleClass().add("column");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<Account, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setMinWidth(300);
        emailColumn.getStyleClass().add("column");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        TableColumn<Account, Void> optionColumn = new TableColumn<>("Option");
        optionColumn.setMinWidth(150);
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
                    optionBox.setSpacing(20);
                    optionBox.setStyle("-fx-alignment: CENTER;");
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
        accountTable.getColumns().addAll(idColumn, nameColumn, usernameColumn,emailColumn, optionColumn);

        // ObservableList to hold Account data
        AccountModel accountModel = new AccountModel();
        accountList = FXCollections.observableArrayList();
        accountList.setAll(accountModel.loadAccounts("")); // Load tất cả tài khoản
        totalPages = (int) Math.ceil((double) accountList.size() / itemsPerPage);

        // Bind the data to the TableView
        accountTable.setItems(accountList);
        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        Label pageLabel = new Label("Page 1 / " + totalPages);
        pageLabel.getStyleClass().add("text-pagination");

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages); // Cập nhật số trang
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages); // Cập nhật số trang
            }
        });


        // HBox chứa các nút phân trang và nhãn số trang
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");

        // Thêm các thành phần vào VBox
        this.getChildren().addAll(titleLabel, topControls, accountTable, paginationBox);
        this.getStyleClass().add("vbox");
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
    // Hàm cập nhật dữ liệu bảng theo trang hiện tại
    private void updateTableData() {
        // Tính toán chỉ số bắt đầu và kết thúc của danh sách tài khoản cho trang hiện tại
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, accountList.size());

        // Cập nhật danh sách đã phân trang
        paginatedAccountList.setAll(accountList.subList(fromIndex, toIndex));

        // Đặt dữ liệu đã phân trang vào TableView
        ((TableView<Account>) getChildren().get(3)).setItems(paginatedAccountList);

        // Cập nhật nhãn số trang
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
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
        Label emailLabel = new Label("Email:"); // Thêm Label cho email
        TextField emailField = new TextField(account.getEmail()); // Thêm TextField cho email
        Label emailErrorLabel = new Label(); // Thêm Label cho lỗi email
        emailErrorLabel.setTextFill(Color.RED);


        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            String fullName = nameComboBox.getValue();
            String newUsername = usernameField.getText();
            String oldPassword = oldPasswordField.getText();
            String newPassword = passwordField.getText();
            String role = account.getRole();
            String email = emailField.getText();

            // Đặt lại các thông báo lỗi
            nameErrorLabel.setText("");
            usernameErrorLabel.setText("");
            oldPasswordErrorLabel.setText("");
            passwordErrorLabel.setText("");
            emailErrorLabel.setText("");

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
            account.setEmail(email);


            boolean success = accountModel.updateAccount(account);
            if (success) {
                editStage.close();
                Stage stage = new Stage();
                EditSuccess message = new EditSuccess();
                message.start(stage);
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
                oldPasswordLabel, oldPasswordField, oldPasswordErrorLabel, passwordLabel, passwordField, passwordErrorLabel,emailLabel,
                emailField, emailErrorLabel,
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

        Label emailLabel = new Label("Email:"); // Thêm Label cho email
        TextField emailField = new TextField(); // Thêm TextField cho email
        Label emailErrorLabel = new Label(); // Thêm Label cho lỗi email
        emailErrorLabel.setTextFill(Color.RED);

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
            String email = emailField.getText();

            nameErrorLabel.setText("");
            usernameErrorLabel.setText("");
            passwordErrorLabel.setText("");
            confirmPasswordErrorLabel.setText("");
            emailErrorLabel.setText("");

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

            if (email.isEmpty()) {
                emailErrorLabel.setText("Email is required.");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            Account newAccount = new Account(0, fullName, username, password ,email);

            boolean success = accountModel.addAccount(newAccount);

            if (success) {
                Stage stage = new Stage();
                AdditionSuccess message = new AdditionSuccess();
                message.start(stage);
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
                emailLabel, emailField, emailErrorLabel,
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
                    Stage stage = new Stage();
                    DeletionSuccess message = new DeletionSuccess();
                    message.start(stage);
                    ((TableView<Account>) getChildren().get(3)).refresh();
                } else {
                    Stage stage = new Stage();
                    DeletionFailed message = new DeletionFailed();
                    message.start(stage);
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
