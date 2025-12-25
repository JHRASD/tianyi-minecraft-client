package com.example.tianyiclient.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestModule {
    private static final Logger LOGGER = LoggerFactory.getLogger("TestModule");
    private final String name = "测试模块";
    private boolean enabled = false;

    public TestModule() {
        LOGGER.info("🎵 创建测试模块: {}", name);
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // 添加 toggle 方法
    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {
        LOGGER.info("🎵 测试模块已启用!");
    }

    public void onDisable() {
        LOGGER.info("🎵 测试模块已禁用!");
    }
}