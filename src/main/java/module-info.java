module com.person98.localminepanel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires eu.hansolo.tilesfx;

    opens com.person98.localminepanel to javafx.fxml;
    exports com.person98.localminepanel;
}