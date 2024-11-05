package view.buttondashboardwarehouse;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ImportView extends VBox {
    public ImportView() {
        Label label = new Label("This is the Import view.");
        getChildren().add(label);
    }
}