package view;

import controller.Session;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Director extends Application {
    @Override
    public void start(Stage primaryStage) {
        if (!Session.isLoggedIn()) {
            try {
                new Login().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
            return;
        }
        HBox root = new HBox();
        root.setPadding(new Insets(10, 50, 10, 50));
        root.setSpacing(20);

        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(getClass().getResource("cashier.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("General Director App");
        primaryStage.setResizable(false);
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);
        primaryStage.show();
    }
}
