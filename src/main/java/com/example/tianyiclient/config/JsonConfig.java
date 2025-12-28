package com.example.tianyiclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class JsonConfig {
    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    protected Path configPath;
    protected ConfigVersion versionInfo;

    public JsonConfig(Path configPath) {
        this.configPath = configPath;
        this.versionInfo = new ConfigVersion();
    }

    public void load() throws IOException {
        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                // 检查版本
                if (json.has("version")) {
                    int fileVersion = json.get("version").getAsInt();
                    if (fileVersion < ConfigVersion.CURRENT_VERSION) {
                        // 需要迁移
                        migrateConfig(json, fileVersion, ConfigVersion.CURRENT_VERSION);
                    }
                }

                fromJson(json);
            }
        } else {
            // 创建默认配置
            save();
        }
    }

    public void save() throws IOException {
        // 更新版本信息
        versionInfo.setLastModified(System.currentTimeMillis());

        // 确保目录存在
        if (configPath.getParent() != null) {
            Files.createDirectories(configPath.getParent());
        }

        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            JsonObject json = toJson();
            GSON.toJson(json, writer);
        }
    }

    protected void migrateConfig(JsonObject json, int fromVersion, int toVersion) {
        // 基础迁移逻辑，子类可以重写
        System.out.println("[Config] 迁移配置从版本 " + fromVersion + " 到 " + toVersion);

        // 这里可以添加自动迁移逻辑
        for (int version = fromVersion; version < toVersion; version++) {
            switch (version) {
                case 1:
                    // v1 -> v2 迁移
                    if (!json.has("clientInfo")) {
                        JsonObject clientInfo = new JsonObject();
                        clientInfo.addProperty("mcVersion", "1.21.8");
                        clientInfo.addProperty("modVersion", "1.0.0");
                        json.add("clientInfo", clientInfo);
                    }
                    break;
                // 添加更多版本迁移...
            }
        }
    }

    protected abstract JsonObject toJson();
    protected abstract void fromJson(JsonObject json);

    public boolean exists() {
        return Files.exists(configPath);
    }

    public Path getConfigPath() {
        return configPath;
    }
}