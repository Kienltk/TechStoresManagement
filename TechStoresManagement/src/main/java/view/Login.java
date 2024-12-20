package view;

import controller.LoginController;
import controller.Session;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.LoginModel;

import java.util.Objects;

public class Login extends Application {

    @Override
    public void start(Stage primaryStage) {
        TextField usernameField = new TextField();
        new LoginModel();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("LOGIN");
        loginButton.setId("login-button");
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/logo.png")));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(150);
        logoView.setFitHeight(150);
        logoView.setPreserveRatio(true);

        double centerX = logoView.getFitWidth() / 2;
        double centerY = logoView.getFitHeight() / 2;
        Circle clip = new Circle(centerX, centerY, 75);
        logoView.setClip(clip);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        VBox vbox = new VBox(10);
        VBox.setMargin(logoView, new Insets(0, 0, 30, 0));
        VBox.setMargin(loginButton, new Insets(10, 0, 0, 0));

        vbox.getChildren().addAll(logoView, usernameField, passwordField, messageLabel, loginButton);
        vbox.setId("login-pane");

        Scene scene = new Scene(vbox, 1366, 768);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("bootstrap-like.css")).toExternalForm());

        primaryStage.setTitle("Login App");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);
        primaryStage.show();

        // Khởi tạo LoginController
        LoginController loginController = new LoginController(usernameField, passwordField, loginButton, messageLabel);

        loginButton.setOnAction(e -> {
            boolean loginSuccess = loginController.handleLogin();

            if (loginSuccess && Session.isLoggedIn()) {
                primaryStage.close();
            }
        });

        passwordField.setOnAction(e -> loginButton.fire());

    }

    public static void main(String[] args) {
        launch(args);
    }
}
