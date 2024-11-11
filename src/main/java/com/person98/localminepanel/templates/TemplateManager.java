package com.person98.localminepanel.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.logging.Logger;

public class TemplateManager {
    private static TemplateManager instance;
    private final Map<String, Map<String, ServerTemplate>> templates = new HashMap<>();
    private final Path templatesPath;
    private static final Logger LOGGER = Logger.getLogger(TemplateManager.class.getName());
    
    private TemplateManager() {
        // Setup templates directory in app data
        String appData = System.getenv("APPDATA");
        Path appDir = Path.of(appData, ".minepanellocal");
        templatesPath = appDir.resolve("templates");
        
        try {
            // Create templates directory if it doesn't exist
            Files.createDirectories(templatesPath);
            
            // Copy default templates from resources
            copyDefaultTemplates();
            
            // Load templates from app data
            loadTemplates();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    
    public static TemplateManager getInstance() {
        if (instance == null) {
            instance = new TemplateManager();
        }
        return instance;
    }
    
    private void copyDefaultTemplates() throws IOException, URISyntaxException {
        // Use the class loader to get resources
        URL url = getClass().getResource("/com/person98/localminepanel/templates");
        if (url == null) {
            System.err.println("Could not find templates directory in resources");
            return;
        }
        
        if (url.getProtocol().equals("jar")) {
            // Handle JAR file
            String path = url.toString();
            path = path.substring("jar:file:/".length(), path.indexOf("!"));
            try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + path), Map.of())) {
                Path root = fs.getPath("/com/person98/localminepanel/templates");
                copyTemplatesFromPath(root);
            }
        } else {
            // Handle directory
            Path root = Paths.get(url.toURI());
            copyTemplatesFromPath(root);
        }
    }
    
    private void copyTemplatesFromPath(Path root) throws IOException {
        Files.walk(root)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> {
                // Get relative path from root
                Path relativePath = root.relativize(path);
                Path targetPath = templatesPath.resolve(relativePath);
                
                try {
                    Files.createDirectories(targetPath.getParent());
                    if (!Files.exists(targetPath)) {
                        Files.copy(path, targetPath);
                        System.out.println("Copied template: " + relativePath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }
    
    private void loadTemplates() {
        LOGGER.info("Loading templates from: " + templatesPath);
        try (DirectoryStream<Path> categories = Files.newDirectoryStream(templatesPath)) {
            for (Path categoryPath : categories) {
                if (Files.isDirectory(categoryPath)) {
                    String category = categoryPath.getFileName().toString();
                    LOGGER.info("Loading category: " + category);
                    templates.put(category, new HashMap<>());
                    
                    try (DirectoryStream<Path> templateFiles = Files.newDirectoryStream(categoryPath, "*.json")) {
                        ObjectMapper mapper = new ObjectMapper();
                        for (Path file : templateFiles) {
                            String templateName = file.getFileName().toString().replace(".json", "");
                            LOGGER.info("Loading template: " + templateName + " from " + file);
                            ServerTemplate template = mapper.readValue(file.toFile(), ServerTemplate.class);
                            templates.get(category).put(templateName, template);
                            LOGGER.info("Successfully loaded template: " + templateName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error loading templates: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ServerTemplate getTemplate(String category, String name) {
        return templates.getOrDefault(category, new HashMap<>()).get(name);
    }
    
    public Map<String, ServerTemplate> getTemplatesForCategory(String category) {
        return templates.getOrDefault(category, new HashMap<>());
    }
    
    public Set<String> getCategories() {
        return templates.keySet();
    }
} 