package com.person98.localminepanel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class MinePanelLocal extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 768);
            scene.getStylesheets().add(getClass().getResource("styles/theme.css").toExternalForm());
            
            stage.setTitle("Mine Panel Local");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Failed to start application", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.toString());
        alert.showAndWait();
    }
} 
