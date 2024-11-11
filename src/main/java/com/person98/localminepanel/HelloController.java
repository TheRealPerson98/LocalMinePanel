package com.person98.localminepanel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HelloController {
    @FXML
    private ListView<String> serverListView;

    @FXML
    public void initialize() {
        ObservableList<String> serverList = FXCollections.observableArrayList();
        serverListView.setItems(serverList);
    }
    
    @FXML
    private void showAddServerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-server-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Add New Server");
            dialogStage.setScene(new Scene(loader.load()));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}