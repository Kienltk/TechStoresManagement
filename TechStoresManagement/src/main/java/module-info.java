module com.example.techstoresmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;

    opens com.example.techstoresmanagement to javafx.fxml;
    exports com.example.techstoresmanagement to javafx.graphics;
}
