package com.person98.localminepanel;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MainView {
    @FXML private ListView<Server> serverListView;
    @FXML private VBox serverDetailsPane;
    private Server selectedServer;

    @FXML
    public void initialize() {
        serverListView.setItems(ServerManager.getInstance().getServers());
        serverListView.setCellFactory(listView -> new ServerListCell());
        
        serverListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedServer = newSelection;
                updateServerDetails(newSelection);
            }
        );
    }

    private void updateServerDetails(Server server) {
        if (server != null) {
            serverDetailsPane.setVisible(true);
            // Update server details display
        } else {
            serverDetailsPane.setVisible(false);
        }
    }
} 