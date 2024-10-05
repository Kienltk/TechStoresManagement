package controller;

import model.LoginModel;
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
        this.messageLabel = messageLabel;

        initialize();
    }

    private void initialize() {
        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return;
        }

        LoginModel loginModel = new LoginModel();
        boolean isValidLogin = loginModel.validateLogin(username, password);

        if (isValidLogin) {
            messageLabel.setText("Login successful!");
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        } else {
            // Đăng nhập thất bại
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }
}