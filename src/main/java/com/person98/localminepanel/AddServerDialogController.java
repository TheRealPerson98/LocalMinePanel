package com.person98.localminepanel;

import com.person98.localminepanel.templates.ServerTemplate;
import com.person98.localminepanel.templates.TemplateManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class AddServerDialogController {
    @FXML private ComboBox<String> serverTypeComboBox;
    @FXML private ComboBox<String> serverSoftwareComboBox;
    @FXML private TextField serverNameField;
    
    @FXML
    public void initialize() {
        setupServerOptions();
        setupListeners();
        serverTypeComboBox.getSelectionModel().selectFirst();
    }
    
    private void setupServerOptions() {
        // Get categories from TemplateManager
        List<String> categories = new ArrayList<>(TemplateManager.getInstance().getCategories());
        serverTypeComboBox.setItems(FXCollections.observableArrayList(categories));
    }
    
    private void setupListeners() {
        serverTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Get templates for selected category
                List<String> templates = new ArrayList<>(
                    TemplateManager.getInstance()
                        .getTemplatesForCategory(newVal.toLowerCase())
                        .keySet()
                );
                serverSoftwareComboBox.setItems(FXCollections.observableArrayList(templates));
            }
        });
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) serverTypeComboBox.getScene().getWindow()).close();
    }
    
    @FXML
    private void handleCreate() {
        String serverType = serverTypeComboBox.getValue();
        String serverSoftware = serverSoftwareComboBox.getValue();
        String serverName = serverNameField.getText();
        
        if (serverName != null && !serverName.trim().isEmpty() &&
            serverType != null && serverSoftware != null) {
            
            Server newServer = new Server(serverName, serverType.toLowerCase(), serverSoftware.toLowerCase());
            
            try {
                // Get the template for the selected server type and software
                ServerTemplate template = TemplateManager.getInstance()
                    .getTemplate(serverType.toLowerCase(), serverSoftware.toLowerCase());
                    
                if (template == null) {
                    showError("Template Error", "Could not find template for selected server type", 
                        new Exception("No template found for " + serverType + "/" + serverSoftware));
                    return;
                }
                
                ServerInstaller.installServer(newServer, template);
                ServerManager.getInstance().addServer(newServer);
                handleCancel();
            } catch (Exception e) {
                showError("Installation Error", "Failed to install server", e);
            }
        }
    }
    
    private void showError(String title, String header, Exception e) {
        Dialog<String> errorDialog = new Dialog<>();
        errorDialog.setTitle(title);
        errorDialog.setHeaderText(header);
        
        TextArea textArea = new TextArea(e.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        errorDialog.getDialogPane().setContent(textArea);
        errorDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        errorDialog.showAndWait();
    }
} 