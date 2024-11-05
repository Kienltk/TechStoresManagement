package view.buttondashboardstore;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GeneralView extends VBox {
    public GeneralView() {
        Label label = new Label("This is the General view.");
        getChildren().add(label);
    }
}