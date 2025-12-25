package com.example.tianyiclient.modules.settings;

import com.example.tianyiclient.settings.Setting;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置分组 - 用于对模块的设置进行逻辑分组
 */
public class SettingGroup {
    private final String name;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean expanded = true; // 默认展开

    /**
     * 创建无名分组
     */
    public SettingGroup() {
        this.name = null;
    }

    /**
     * 创建有名分组
     */
    public SettingGroup(String name) {
        this.name = name;
    }

    /**
     * 添加设置到分组
     */
    public void add(Setting<?> setting) {
        if (setting != null && !settings.contains(setting)) {
            settings.add(setting);
        }
    }

    /**
     * 从分组移除设置
     */
    public void remove(Setting<?> setting) {
        settings.remove(setting);
    }

    /**
     * 获取分组中的所有设置
     */
    public List<Setting<?>> getSettings() {
        return new ArrayList<>(settings);
    }

    /**
     * 获取分组名称（可能为null）
     */
    public String getName() {
        return name;
    }

    /**
     * 检查是否有名称
     */
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    /**
     * 设置分组是否展开（在GUI中）
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * 检查分组是否展开
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * 获取分组中的设置数量
     */
    public int size() {
        return settings.size();
    }

    /**
     * 检查分组是否为空
     */
    public boolean isEmpty() {
        return settings.isEmpty();
    }

    /**
     * 清空分组中的所有设置
     */
    public void clear() {
        settings.clear();
    }
}