package com.person98.localminepanel.services.template.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownloadConfig {
    private String type;
    
    @JsonProperty("base_url")
    private String baseUrl;
    
    @JsonProperty("version_path")
    private String versionPath;
    
    @JsonProperty("latest_version_path")
    private String latestVersionPath;
    
    @JsonProperty("build_url")
    private String buildUrl;
    
    @JsonProperty("build_path")
    private String buildPath;
    
    @JsonProperty("latest_build_path")
    private String latestBuildPath;
    
    @Override
    public String toString() {
        return String.format("DownloadConfig{type='%s', baseUrl='%s', buildUrl='%s'}", type, baseUrl, buildUrl);
    }
}