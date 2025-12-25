package com.example.tianyiclient.hud.config;

import com.example.tianyiclient.TianyiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HudConfig {
    private static final File CONFIG_FILE = new File(
            "config/tianyiclient/hud_config.json"
    );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, Object> config = new HashMap<>();

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            Files.write(CONFIG_FILE.toPath(), GSON.toJson(config).getBytes());
        } catch (Exception e) {
            TianyiClient.LOGGER.error("保存HUD配置失败", e);
        }
    }

    public void load() {
        if (!CONFIG_FILE.exists()) return;
        try {
            String json = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
            config = GSON.fromJson(json, HashMap.class);
        } catch (Exception e) {
            TianyiClient.LOGGER.error("加载HUD配置失败", e);
        }
    }

    public void put(String key, Object value) {
        config.put(key, value);
    }

    public <T> T get(String key, T defaultValue) {
        return (T) config.getOrDefault(key, defaultValue);
    }
}