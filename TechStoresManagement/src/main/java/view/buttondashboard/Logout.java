package view.buttondashboard;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import view.stage.LogoutSuccess;

public class Logout extends VBox {
    public Logout() {
        Label label = new Label("This is the logout view.");
        getChildren().add(label);
    }
}
