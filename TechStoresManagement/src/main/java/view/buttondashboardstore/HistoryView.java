package view.buttondashboardstore;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HistoryView extends VBox {
    public HistoryView() {
        Label label = new Label("This is the History view.");
        getChildren().add(label);
    }
}