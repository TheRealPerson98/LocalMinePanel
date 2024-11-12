package com.person98.localminepanel.application.controllers;

import com.person98.localminepanel.core.Server;
import com.person98.localminepanel.services.installer.ServerInstaller;
import com.person98.localminepanel.core.ServerManager;
import com.person98.localminepanel.services.template.ServerTemplate;
import com.person98.localminepanel.services.template.TemplateManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import java.util.*;

public class AddServerDialogController {
    @FXML private ComboBox<String> serverTypeComboBox;
    @FXML private ComboBox<String> serverSoftwareComboBox;
    @FXML private TextField serverNameField;
    @FXML private VBox serverConfigVBox;
    @FXML private VBox startupConfigVBox;
    
    private Map<String, TextField> configFields = new HashMap<>();
    private Map<String, TextField> startupFields = new HashMap<>();
    
    @FXML
    public void initialize() {
        setupServerOptions();
        setupListeners();
    }
    
    private void setupServerOptions() {
        List<String> categories = new ArrayList<>(TemplateManager.getInstance().getCategories());
        serverTypeComboBox.setItems(FXCollections.observableArrayList(categories));
    }
    
    private void setupListeners() {
        serverTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> templates = new ArrayList<>(
                    TemplateManager.getInstance()
                        .getTemplatesForCategory(newVal.toLowerCase())
                        .keySet()
                );
                serverSoftwareComboBox.setItems(FXCollections.observableArrayList(templates));
            }
        });

        serverSoftwareComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && serverTypeComboBox.getValue() != null) {
                updateConfigurationFields();
            }
        });
    }
    
    private void updateConfigurationFields() {
        ServerTemplate template = TemplateManager.getInstance()
            .getTemplate(serverTypeComboBox.getValue().toLowerCase(), 
                        serverSoftwareComboBox.getValue().toLowerCase());
        
        if (template == null) return;
        
        // Clear existing fields
        serverConfigVBox.getChildren().clear();
        startupConfigVBox.getChildren().clear();
        configFields.clear();
        startupFields.clear();
        
        // Add server.properties configuration fields
        if (template.getConfig() != null && template.getConfig().containsKey("server.properties")) {
            Map<String, String> properties = template.getConfig().get("server.properties");
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                TextField field = new TextField(entry.getValue());
                field.setPromptText(entry.getKey());
                configFields.put(entry.getKey(), field);
                
                Label label = new Label(entry.getKey());
                serverConfigVBox.getChildren().addAll(label, field);
            }
        }
        
        // Add startup configuration fields
        if (template.getStartup() != null && template.getStartup().getVariables() != null) {
            Map<String, String> variables = template.getStartup().getVariables();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                TextField field = new TextField(entry.getValue());
                field.setPromptText(entry.getKey());
                startupFields.put(entry.getKey(), field);
                
                Label label = new Label(entry.getKey());
                startupConfigVBox.getChildren().addAll(label, field);
            }
        }
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
                ServerTemplate template = TemplateManager.getInstance()
                    .getTemplate(serverType.toLowerCase(), serverSoftware.toLowerCase());
                    
                if (template == null) {
                    showError("Template Error", "Could not find template for selected server type", 
                        new Exception("No template found for " + serverType + "/" + serverSoftware));
                    return;
                }
                
                // Apply configuration from fields
                applyConfiguration(newServer, template);
                
                ServerInstaller.installServer(newServer, template);
                ServerManager.getInstance().addServer(newServer);
                handleCancel();
            } catch (Exception e) {
                showError("Installation Error", "Failed to install server", e);
            }
        }
    }
    
    private void applyConfiguration(Server server, ServerTemplate template) {
        // Apply startup configuration
        for (Map.Entry<String, TextField> entry : startupFields.entrySet()) {
            String value = entry.getValue().getText();
            if (entry.getKey().equals("memory")) {
                server.setMemory(Integer.parseInt(value));
            } else if (entry.getKey().equals("java_args")) {
                server.setJavaArgs(value);
            }
        }
        
        // Store server.properties configuration in template
        Map<String, Map<String, String>> config = template.getConfig();
        Map<String, String> properties = config.get("server.properties");
        for (Map.Entry<String, TextField> entry : configFields.entrySet()) {
            properties.put(entry.getKey(), entry.getValue().getText());
        }
        
        // Save the initial startup command from template
        if (template.getStartup() != null) {
            server.setStartupCommand(template.getStartup().getCommand());
        }
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) serverTypeComboBox.getScene().getWindow()).close();
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