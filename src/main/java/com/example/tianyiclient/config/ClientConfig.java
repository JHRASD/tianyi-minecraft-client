package com.example.tianyiclient.config;

import com.google.gson.JsonObject;
import java.nio.file.Path;

public class ClientConfig extends JsonConfig {
    private String theme = "dark";
    private int guiColor = 0x2F3136;
    private int accentColor = 0x5865F2;
    private boolean showWelcomeMessage = true;

    public ClientConfig(Path configPath) {
        super(configPath);
    }

    @Override
    protected JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", versionInfo.getVersion());
        root.addProperty("lastModified", versionInfo.getLastModified());
        root.addProperty("theme", theme);
        root.addProperty("guiColor", guiColor);
        root.addProperty("accentColor", accentColor);
        root.addProperty("showWelcomeMessage", showWelcomeMessage);
        return root;
    }

    @Override
    protected void fromJson(JsonObject json) {
        if (json.has("theme")) {
            theme = json.get("theme").getAsString();
        }
        if (json.has("guiColor")) {
            guiColor = json.get("guiColor").getAsInt();
        }
        if (json.has("accentColor")) {
            accentColor = json.get("accentColor").getAsInt();
        }
        if (json.has("showWelcomeMessage")) {
            showWelcomeMessage = json.get("showWelcomeMessage").getAsBoolean();
        }
    }
}