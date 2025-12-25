// 如果 SettingGroup.java 没有 remove 方法，添加它：
package com.example.tianyiclient.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingGroup {
    private final List<Setting<?>> settings = new ArrayList<>();
    private String name = "设置";

    public SettingGroup() {}

    public SettingGroup(String name) {
        this.name = name;
    }

    public void add(Setting<?> setting) {
        if (!settings.contains(setting)) {
            settings.add(setting);
        }
    }

    // 添加 remove 方法
    public void remove(Setting<?> setting) {
        settings.remove(setting);
    }

    // 添加根据名称移除的方法
    public void removeByName(String name) {
        settings.removeIf(setting -> setting.getName().equals(name));
    }

    public List<Setting<?>> getSettings() {
        return new ArrayList<>(settings);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmpty() {
        return settings.isEmpty();
    }

    public int size() {
        return settings.size();
    }
}