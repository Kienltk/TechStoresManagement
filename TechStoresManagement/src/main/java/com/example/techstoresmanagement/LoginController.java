package com.example.techstoresmanagement;

import javafx.scene.control.*;

public class LoginController {


    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label messageLabel;


    public LoginController(TextField usernameField, PasswordField passwordField, Button loginButton, Label messageLabel) {
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.loginButton = loginButton;
        this.messageLabel = messageLabel; // Khởi tạo messageLabel

        initialize();
    }

    private void initialize() {
        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Kiểm tra nếu username hoặc password để trống
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setVisible(true); // Hiện thông báo
            messageLabel.setManaged(true); // Cho phép chiếm diện tích
            return;
        }

        // Gọi đến lớp LoginModel để kiểm tra thông tin đăng nhập
        LoginModel loginModel = new LoginModel();
        boolean isValidLogin = loginModel.validateLogin(username, password);

        if (isValidLogin) {
            // Đăng nhập thành công
            messageLabel.setText("Login successful!");
            messageLabel.setStyle("-fx-text-fill: green;"); // Đổi màu thành xanh
            messageLabel.setVisible(true); // Hiện thông báo
            messageLabel.setManaged(true); // Cho phép chiếm diện tích
        } else {
            // Đăng nhập thất bại
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true); // Hiện thông báo
            messageLabel.setManaged(true); // Cho phép chiếm diện tích
        }
    }
}