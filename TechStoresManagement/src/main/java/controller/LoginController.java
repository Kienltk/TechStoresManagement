package controller;

import javafx.stage.Stage;
import model.LoginModel;
import javafx.scene.control.*;
import view.Cashier;
import view.Director;
import view.StoreManager;
import view.WarehouseManager;

import java.util.Map;

public class LoginController {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label messageLabel;
    private LoginModel loginModel = new LoginModel();

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

        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return false;
        }

        boolean isValidLogin = loginModel.validateLogin(username, password);

        if (isValidLogin) {
            messageLabel.setText("Login successful!");
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);

            Map<String, Object> userInfo = loginModel.getRoleAndLocation(username);
            String role = (String) userInfo.get("role");
            int idStore = (int) userInfo.get("id_store");
            int idWarehouse = (int) userInfo.get("id_warehouse");
            String employeeName = (String) userInfo.get("employee_name");

            // Lưu thông tin vào Session
            Session.setLoggedIn(true);
            Session.setRole(role);
            Session.setIdStore(idStore);
            Session.setIdWarehouse(idWarehouse);
            Session.setEmployeeName(employeeName);

            // Điều hướng dựa trên role
            switch (role) {
                case "General Director":
                    new Director().start(new Stage());
                    break;
                case "Store Management":
                    new StoreManager().start(new Stage());
                    break;
                case "Warehouse Management":
                    new WarehouseManager().start(new Stage());
                    break;
                case "Cashier":
                    new Cashier().start(new Stage());
                    break;
                default:
                    System.out.println("Unknown role");
            }
            return true;
        } else {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return false;
        }
    }


}
