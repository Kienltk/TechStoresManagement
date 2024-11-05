package view.buttondashboardstore;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProductView extends VBox {
    public ProductView() {
        Label label = new Label("This is the Product view.");
        getChildren().add(label);
    }
}