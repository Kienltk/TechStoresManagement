package view.buttondashboard;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class WarehouseManagementView extends VBox {
    public WarehouseManagementView() {
        Label label = new Label("This is the warehouse view.");
        getChildren().add(label);
    }
}
