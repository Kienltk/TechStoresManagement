package view.buttondashboardwarehouse;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
public class EmployeeView extends VBox {
    public EmployeeView() {
        Label label = new Label("This is the Employee Management view.");
        getChildren().add(label);
    }
}