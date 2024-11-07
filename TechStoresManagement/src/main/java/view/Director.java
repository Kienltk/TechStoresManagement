package view;

import controller.DirectorController;
import controller.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;

import javafx.stage.Stage;

import java.util.Objects;

public class Director extends Application {

    private final String employeeName;

    public Director() {
        this.employeeName = Session.getEmployeeName();
    }

    @Override
    public void start(Stage primaryStage)throws Exception {
        if (!Session.isLoggedIn()) {
            try {
                new Login().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
            return;
        }
        System.out.println(employeeName);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("director.css")).toExternalForm());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            DirectorController.deleteTempProductImage();
        });
    }
}
