package com.person98.localminepanel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.person98.localminepanel.templates.DownloadConfig;
import com.person98.localminepanel.templates.ServerTemplate;
import com.person98.localminepanel.templates.TemplateManager;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class ServerInstaller {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger LOGGER = Logger.getLogger(ServerInstaller.class.getName());
    
    static {
        try {
            // Setup logging to file
            String logPath = System.getenv("APPDATA") + "\\.minepanellocal\\installer.log";
            FileHandler fh = new FileHandler(logPath, true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installServer(Server server, ServerTemplate template) throws Exception {
        LOGGER.info("Starting server installation for " + server.getName());
        LOGGER.info("Server type: " + server.getType() + ", Software: " + server.getSoftware());
        
        server.setTemplate(template);
        
        LOGGER.info("Template loaded successfully: " + template.getName());
        LOGGER.info("Version URL: " + template.getVersionUrl());
        
        Path serverDir = Path.of(server.getServerPath());
        Files.createDirectories(serverDir);
        LOGGER.info("Created server directory at: " + serverDir);
        
        downloadServerJar(server, template, serverDir);
        createConfigFiles(server, template, serverDir);
        createStartScripts(server, template, serverDir);
        
        Path eulaPath = serverDir.resolve("eula.txt");
        Files.writeString(eulaPath, "eula=true");
        LOGGER.info("Server installation completed successfully");
    }

    private static void downloadServerJar(Server server, ServerTemplate template, Path serverDir) throws Exception {
        LOGGER.info("Starting server JAR download process");
        String versionUrl = template.getVersionUrl();
        DownloadConfig download = template.getDownload();
        
        LOGGER.info("Download config: " + mapper.writeValueAsString(download));
        
        // Get version info
        HttpRequest versionRequest = HttpRequest.newBuilder()
            .uri(new URI(versionUrl))
            .GET()
            .build();
            
        LOGGER.info("Sending version request to: " + versionUrl);
        String versionResponse = client.send(versionRequest, HttpResponse.BodyHandlers.ofString()).body();
        LOGGER.info("Version response received: " + versionResponse);
        
        JsonNode versionJson = mapper.readTree(versionResponse);
        JsonNode versionsNode = versionJson.get("versions");
        
        // Get the latest version
        String version = versionsNode.get(versionsNode.size() - 1).asText();
        LOGGER.info("Selected version: " + version);
        
        // Get build info
        String buildUrl = versionUrl + "/" + download.getBuildUrl().replace("{version}", version);
        LOGGER.info("Build URL: " + buildUrl);
        
        HttpRequest buildRequest = HttpRequest.newBuilder()
            .uri(new URI(buildUrl))
            .GET()
            .build();
            
        String buildResponse = client.send(buildRequest, HttpResponse.BodyHandlers.ofString()).body();
        LOGGER.info("Build response: " + buildResponse);
        
        JsonNode buildJson = mapper.readTree(buildResponse);
        JsonNode buildsNode = buildJson.get("builds");
        String build = buildsNode.get(buildsNode.size() - 1).asText();
        LOGGER.info("Selected build: " + build);
        
        // Construct download URL
        String downloadUrl = download.getBaseUrl()
            .replace("{version}", version)
            .replace("{build}", build);
        LOGGER.info("Download URL: " + downloadUrl);
        
        // Download the file
        HttpRequest downloadRequest = HttpRequest.newBuilder()
            .uri(new URI(downloadUrl))
            .GET()
            .build();
            
        Path jarPath = serverDir.resolve("server.jar");
        client.send(downloadRequest, HttpResponse.BodyHandlers.ofFile(jarPath));
        
        // Set the server jar path
        server.setServerJar("server.jar");
    }

    private static void createConfigFiles(Server server, ServerTemplate template, Path serverDir) throws IOException {
        for (Map.Entry<String, Map<String, String>> entry : template.getConfig().entrySet()) {
            String fileName = entry.getKey();
            Map<String, String> properties = entry.getValue();
            
            if (fileName.equals("server.properties")) {
                Properties props = new Properties();
                for (Map.Entry<String, String> prop : properties.entrySet()) {
                    props.setProperty(prop.getKey(), prop.getValue()
                        .replace("{port}", String.valueOf(server.getPort()))
                        .replace("{ip}", server.getIp()));
                }
                
                Path propsPath = serverDir.resolve(fileName);
                try (OutputStream out = Files.newOutputStream(propsPath)) {
                    props.store(out, "Minecraft server properties");
                }
            }
        }
    }

    private static void createStartScripts(Server server, ServerTemplate template, Path serverDir) throws IOException {
        String command = template.getStartup().getCommand()
            .replace("{memory}", String.valueOf(server.getMemory()))
            .replace("{java_args}", server.getJavaArgs())
            .replace("{server_jar}", server.getServerJar());

        // Create Windows batch script
        String batchScript = "@echo off\n" + command + "\npause";
        Files.writeString(serverDir.resolve("start.bat"), batchScript);

        // Create shell script
        String shScript = "#!/bin/sh\n" + command;
        Path shPath = serverDir.resolve("start.sh");
        Files.writeString(shPath, shScript);
        shPath.toFile().setExecutable(true);
    }
} 