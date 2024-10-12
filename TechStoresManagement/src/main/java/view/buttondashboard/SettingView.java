package view.buttondashboard;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class SettingView extends VBox {
    public SettingView() {
        Label label = new Label("This is the Setting view.");
        getChildren().add(label);
    }
}
