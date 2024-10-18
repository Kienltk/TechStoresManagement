package view.stage;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;

public class AdditionSuccess extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Addition Success");

        // Title
        Label titleLabel = new Label("Addition");
        titleLabel.setFont(Font.font("Roboto", FontWeight.EXTRA_BOLD, 24));
        titleLabel.setTextFill(Color.BLACK);

        // Icon``
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/success.jpg")).toString()));
        icon.setFitWidth(60);
        icon.setFitHeight(60);

        // Message
        Label messageLabel = new Label("Addition success");
        messageLabel.setFont(Font.font("Roboto", 20));
        messageLabel.setTextFill(Color.BLACK);

        // Button
        Button returnButton = new Button("Return");
        returnButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white; -fx-background-radius: 25px;");
        returnButton.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 15));
        returnButton.setPadding(new Insets(10, 20, 10, 20));

        // Hover effect
        returnButton.setOnMouseEntered(e -> returnButton.setStyle("-fx-background-color: #38a9ad; -fx-text-fill: white; -fx-background-radius: 25px;"));
        returnButton.setOnMouseExited(e -> returnButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white; -fx-background-radius: 25px;"));

        // Layout
        VBox vbox = new VBox(30, titleLabel, icon, messageLabel, returnButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        vbox.setPadding(new Insets(20));
        vbox.setPrefSize(300, 400); // Cố định kích thước khung ảnh
// Fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), vbox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.setWidth(240);
        primaryStage.setHeight(320);
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();


        // Tạo hiệu ứng đóng cửa sổ
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), vbox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> primaryStage.close());

        // Sự kiện cho nút "Return" để đóng cửa sổ với hiệu ứng
        returnButton.setOnAction(e -> fadeOut.play());

        // Tự động đóng cửa sổ sau 10 giây với hiệu ứng
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(10), event -> fadeOut.play()));
        autoClose.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
