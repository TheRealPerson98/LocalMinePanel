module com.person98.localminepanel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires eu.hansolo.tilesfx;

    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires jdk.management;
    requires static lombok;
    requires java.logging;

    opens com.person98.localminepanel to javafx.fxml;
    opens com.person98.localminepanel.templates to com.fasterxml.jackson.databind;
    exports com.person98.localminepanel;
    exports com.person98.localminepanel.templates;
}