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
    opens com.person98.localminepanel.services.template to com.fasterxml.jackson.databind;
    opens com.person98.localminepanel.core to java.base;
    exports com.person98.localminepanel;
    exports com.person98.localminepanel.services.template;
    exports com.person98.localminepanel.application.views;
    exports com.person98.localminepanel.core;
    opens com.person98.localminepanel.application.views to javafx.fxml;
    exports com.person98.localminepanel.application.controllers;
    opens com.person98.localminepanel.application.controllers to javafx.fxml;
    exports com.person98.localminepanel.services.file;
    opens com.person98.localminepanel.services.file to javafx.fxml;
    exports com.person98.localminepanel.services.installer;
    opens com.person98.localminepanel.services.installer to javafx.fxml;
    exports com.person98.localminepanel.services.template.models;
    opens com.person98.localminepanel.services.template.models to com.fasterxml.jackson.databind;
}