package view.buttondashboard;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class GeneralView extends VBox {
    public GeneralView() {
        Label label = new Label("This is the General view.");
        getChildren().add(label);
    }
}