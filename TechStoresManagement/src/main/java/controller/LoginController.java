package controller;

import model.LoginModel;
import javafx.scene.control.*;
import view.Session;

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

    public boolean handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Kiểm tra nếu tên đăng nhập hoặc mật khẩu bị bỏ trống
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return false; // Đăng nhập không hợp lệ
        }

        // Giả sử LoginModel kiểm tra thông tin đăng nhập
        LoginModel loginModel = new LoginModel();
        boolean isValidLogin = loginModel.validateLogin(username, password);

        if (isValidLogin) {
            // Đăng nhập thành công
            messageLabel.setText("Login successful!");
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);

            // Cập nhật trạng thái đăng nhập
            Session.setLoggedIn(true);
            return true; // Đăng nhập hợp lệ
        } else {
            // Đăng nhập thất bại
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return false; // Đăng nhập không hợp lệ
        }
    }
}
