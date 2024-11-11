package com.person98.localminepanel;

import javafx.scene.control.ListCell;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;

public class ServerListCell extends ListCell<Server> {
    private final HBox content;
    private final Text text;
    private final Button deleteButton;

    public ServerListCell() {
        text = new Text();
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
    }

    @Override
    protected void updateItem(Server server, boolean empty) {
        super.updateItem(server, empty);
        
        if (empty || server == null) {
            setGraphic(null);
        } else {
            text.setText(server.getName() + (server.isRunning() ? " (Running)" : " (Stopped)"));
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
        }
    }
} 