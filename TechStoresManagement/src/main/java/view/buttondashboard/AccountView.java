package view.buttondashboard;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class AccountView extends VBox {
    public AccountView() {
        Label label = new Label("This is the Setting view.");
        getChildren().add(label);
    }
}
