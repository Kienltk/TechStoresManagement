package view.buttondashboard;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class ProductManagementView extends VBox {
    public ProductManagementView() {
        Label label = new Label("This is the Product Management view.");
        getChildren().add(label);
    }
}
