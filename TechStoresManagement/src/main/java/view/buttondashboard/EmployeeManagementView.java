package view.buttondashboard;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
public class EmployeeManagementView extends VBox {
    public EmployeeManagementView() {
        Label label = new Label("This is the Employee Management view.");
        getChildren().add(label);
    }
}
