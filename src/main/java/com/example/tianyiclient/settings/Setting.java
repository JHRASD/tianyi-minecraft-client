package com.example.tianyiclient.settings;

import com.example.tianyiclient.config.ConfigManager;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    private T value;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public T getValue() { return value; }

    public void setValue(T value) {
        if (this.value != value) {
            this.value = value;
            // 标记配置需要保存
            ConfigManager.getInstance().markDirty();
        }
    }
}