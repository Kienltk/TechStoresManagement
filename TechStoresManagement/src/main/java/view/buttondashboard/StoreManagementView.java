package view.buttondashboard;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class StoreManagementView extends VBox {
    public StoreManagementView() {
        Label label = new Label("This is the Store Management view.");
        getChildren().add(label);
    }
}
