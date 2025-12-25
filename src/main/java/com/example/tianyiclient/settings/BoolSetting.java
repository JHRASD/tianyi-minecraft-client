package com.example.tianyiclient.settings;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean isEnabled() {
        Boolean val = getValue();
        return val != null && val;
    }

    public void setEnabled(boolean enabled) {
        setValue(enabled);
    }

    public void toggle() {
        setValue(!isEnabled());
    }
}
/**
 * 布尔设置 - 用于创建开关/复选框设置
 * 功能：
 * - 提供 true/false 开关
 * - 支持切换操作
 * - 提供便捷的启用检查方法
 *
 * 使用示例：
 * - 显示坐标：true/false
 * - 自动跳跃：开启/关闭
 * - 粒子效果：启用/禁用
 */