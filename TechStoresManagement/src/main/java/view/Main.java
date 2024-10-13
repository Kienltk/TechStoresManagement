package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class Main extends Application {
    //    public static void main(String[] args) {
//        Login.main(args);
//    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Account.fxml"));
        primaryStage.setTitle("Employee Management");
        primaryStage.setScene(new Scene(root, 1366, 768));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
