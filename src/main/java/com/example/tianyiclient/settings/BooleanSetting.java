package com.example.tianyiclient.settings;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, Boolean defaultValue) {
        super(name, description, defaultValue);
    }

    // 添加便捷方法
    public boolean getValueBoolean() {
        return getValue() != null ? getValue() : false;
    }

    public void setValueBoolean(boolean value) {
        setValue(value);
    }
}