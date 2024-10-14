package view.buttondashboard;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
public class ImportProductView extends VBox {
    public ImportProductView() {
        Label label = new Label("This is the import product view.");
        getChildren().add(label);
    }
}
