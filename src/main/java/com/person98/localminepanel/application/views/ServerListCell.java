package com.person98.localminepanel.application.views;

import com.person98.localminepanel.core.Server;
import com.person98.localminepanel.core.ServerManager;
import javafx.scene.control.ListCell;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

public class ServerListCell extends ListCell<Server> {
    private final HBox content;
    private final Text text;
    private final Button deleteButton;
    private Server currentServer;
    private Timeline updateTimeline;

    public ServerListCell() {
        text = new Text();
        text.setStyle("-fx-fill: white;");
        deleteButton = new Button();
        
        // Create trash can icon
        FontAwesomeIconView trashIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        trashIcon.setStyle("-fx-fill: #ff4444;");
        deleteButton.setGraphic(trashIcon);
        deleteButton.getStyleClass().add("delete-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        content = new HBox(5);
        content.getChildren().addAll(text, spacer, deleteButton);

        // Create timeline for updates
        updateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> updateStatus())
        );
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    protected void updateItem(Server server, boolean empty) {
        super.updateItem(server, empty);
        
        // Stop timeline for old server
        updateTimeline.stop();
        currentServer = server;
        
        if (empty || server == null) {
            setGraphic(null);
        } else {
            updateStatus();
            deleteButton.setOnAction(e -> {
                if (server.isRunning()) {
                    try {
                        server.stop();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                ServerManager.getInstance().removeServer(server);
            });
            setGraphic(content);
            updateTimeline.play();
        }
    }

    private void updateStatus() {
        if (currentServer == null) return;
        
        Platform.runLater(() -> {
            String status;
            try {
                if (currentServer.isRunning()) {
                    status = " (Online)";
                } else if (currentServer.getProcess() != null) {
                    status = " (Starting)";
                } else {
                    status = " (Offline)";
                }
            } catch (Exception e) {
                status = " (Offline)";
            }
            text.setText(currentServer.getName() + status);
        });
    }
} 