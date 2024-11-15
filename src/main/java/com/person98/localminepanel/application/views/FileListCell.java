package com.person98.localminepanel.application.views;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class FileListCell extends ListCell<String> {
    private final HBox content;
    private final CheckBox checkBox;
    private final Label label;

    public FileListCell() {
        content = new HBox(5);
        checkBox = new CheckBox();
        label = new Label();
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        content.getChildren().addAll(checkBox, label);
        
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (getItem() != null) {
                getListView().getProperties().put("selected_" + getItem(), newVal);
                updateFileActionsVisibility();
            }
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setGraphic(null);
        } else {
            label.setText(item);
            Boolean isSelected = (Boolean) getListView().getProperties().get("selected_" + item);
            checkBox.setSelected(isSelected != null && isSelected);
            setGraphic(content);
        }
    }

    private void updateFileActionsVisibility() {
        boolean anySelected = getListView().getItems().stream()
            .anyMatch(item -> {
                Boolean selected = (Boolean) getListView().getProperties().get("selected_" + item);
                return selected != null && selected;
            });
        
        HBox fileActionsBox = (HBox) getScene().lookup("#fileActionsBox");
        if (fileActionsBox != null) {
            fileActionsBox.setVisible(anySelected);
        }
    }
} 