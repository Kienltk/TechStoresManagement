module techstoresmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;
    exports controller to javafx.graphics;
    opens controller to javafx.fxml;
    exports view.buttondashboard;
    exports model to javafx.graphics;
    exports view.stage to javafx.graphics;
    opens model to javafx.fxml;
    exports view to javafx.graphics;
    opens view to javafx.fxml;

}
