module com.example.techstoresmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;

    exports controller to javafx.graphics;
    opens controller to javafx.fxml;
    exports model to javafx.graphics;
    opens model to javafx.fxml;
    exports side to javafx.graphics;
    opens side to javafx.fxml;

}
