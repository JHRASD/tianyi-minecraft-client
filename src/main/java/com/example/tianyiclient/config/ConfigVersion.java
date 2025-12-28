package com.example.tianyiclient.config;

public class ConfigVersion {
    public static final int CURRENT_VERSION = 1;

    private int version;
    private long lastModified;
    private String mcVersion;
    private String clientVersion;

    public ConfigVersion() {
        this.version = CURRENT_VERSION;
        this.lastModified = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public String getMcVersion() { return mcVersion; }
    public void setMcVersion(String mcVersion) { this.mcVersion = mcVersion; }
    public String getClientVersion() { return clientVersion; }
    public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }
}