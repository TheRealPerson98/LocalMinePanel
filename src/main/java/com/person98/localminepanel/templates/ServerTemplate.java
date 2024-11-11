package com.person98.localminepanel.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerTemplate {
    private String name;
    private String author;
    private String description;
    @JsonProperty("version_url")
    private String versionUrl;
    private DownloadConfig download;
    private Map<String, Map<String, String>> config;
    private StartupConfig startup;
    private StopConfig stop;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StartupConfig {
        private String command;
        private String doneRegex;
        private Map<String, String> variables;
    }
    
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StopConfig {
        private String command;
        private int timeout;
    }
} 