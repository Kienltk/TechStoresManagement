package view;

import controller.Session;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class WarehouseManager extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private int idWarehouse;
    private String employeeName;

    public WarehouseManager(int idStore) {
        // Lấy thông tin từ Session
        this.idWarehouse = Session.getIdStore();
        this.employeeName = Session.getEmployeeName();
    }

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
        System.out.println("Logged in as: " + employeeName + " at Warehouse ID: " + idWarehouse);

        HBox root = new HBox();
        root.setPadding(new Insets(10, 50, 10, 50));
        root.setSpacing(20);

        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(getClass().getResource("cashier.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Warehouse Management App");
        primaryStage.setResizable(false);
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);
        primaryStage.show();
    }
}
