package com.example.tianyiclient.config;

import com.example.tianyiclient.TianyiClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigManager {
    private static ConfigManager instance;

    private final Path configDir;
    private ModuleConfig moduleConfig;
    private HudConfig hudConfig;
    private ClientConfig clientConfig;

    private boolean dirty = false;
    private long lastSaveTime = 0;
    private static final long AUTO_SAVE_INTERVAL = 10000; // 10秒

    private ConfigManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir().resolve("TianyiClient");

        // 初始化各个配置
        this.moduleConfig = new ModuleConfig(configDir.resolve("modules.json"));
        this.hudConfig = new HudConfig(configDir.resolve("hud.json"));
        this.clientConfig = new ClientConfig(configDir.resolve("client.json"));

        // 注册生命周期事件
        registerEvents();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void registerEvents() {
        // 客户端启动时加载配置
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            loadAllConfigs();
        });

        // 客户端停止时保存配置
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            saveAllConfigs();
        });

        // 定期自动保存
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (dirty && System.currentTimeMillis() - lastSaveTime > AUTO_SAVE_INTERVAL) {
                saveAllConfigs();
                dirty = false;
            }
        });
    }

    public void loadAllConfigs() {
        try {
            TianyiClient.LOGGER.info("加载配置...");

            // 加载模块配置
            moduleConfig.loadToManager();

            // 加载HUD配置
            hudConfig.loadToManager();

            // 加载客户端配置
            clientConfig.load();

            TianyiClient.LOGGER.info("配置加载完成");
        } catch (Exception e) {
            TianyiClient.LOGGER.error("加载配置失败", e);
        }
    }

    public void saveAllConfigs() {
        try {
            TianyiClient.LOGGER.info("保存配置...");

            // 保存模块配置
            moduleConfig.saveFromManager();

            // 保存HUD配置
            hudConfig.saveFromManager();

            // 保存客户端配置
            clientConfig.save();

            lastSaveTime = System.currentTimeMillis();
            dirty = false;

            TianyiClient.LOGGER.info("配置保存完成");
        } catch (Exception e) {
            TianyiClient.LOGGER.error("保存配置失败", e);
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    // Getter方法
    public ModuleConfig getModuleConfig() { return moduleConfig; }
    public HudConfig getHudConfig() { return hudConfig; }
    public ClientConfig getClientConfig() { return clientConfig; }

    public void saveNow() {
        saveAllConfigs();
    }

    public void reload() {
        loadAllConfigs();
    }
}