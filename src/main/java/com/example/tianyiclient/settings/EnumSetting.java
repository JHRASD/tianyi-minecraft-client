package com.example.tianyiclient.settings;

import java.util.Arrays;
import java.util.List;

/**
 * 枚举设置 - 用于创建下拉选择框
 * 例如：模式选择【正常模式|激进模式|隐身模式】
 *       渲染模式【填充|线框|隐藏】
 */
public class EnumSetting extends Setting<String> {
    private final List<String> modes;
    private String value;
    private int index;

    public EnumSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue);
        this.modes = Arrays.asList(modes);
        this.value = defaultValue;
        this.index = this.modes.indexOf(defaultValue);

        // 确保默认值在选项中 - 修复：检查数组长度而不是调用方法
        if (this.index == -1 && modes.length > 0) {
            this.value = modes[0];
            this.index = 0;
        }
    }

    // 简化构造函数（没有描述）
    public EnumSetting(String name, String defaultValue, String... modes) {
        this(name, "", defaultValue, modes);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (modes.contains(value)) {
            this.value = value;
            this.index = modes.indexOf(value);
        }
    }

    /**
     * 获取当前选项的索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置当前选项（通过索引）
     */
    public void setIndex(int index) {
        if (index >= 0 && index < modes.size()) {
            this.index = index;
            this.value = modes.get(index);
        }
    }

    /**
     * 获取所有可用的选项
     */
    public List<String> getModes() {
        return modes;
    }

    /**
     * 循环切换到下一个选项
     */
    public void cycle() {
        index = (index + 1) % modes.size();
        value = modes.get(index);
    }

    /**
     * 循环切换到上一个选项
     */
    public void cycleBack() {
        index = (index - 1 + modes.size()) % modes.size();
        value = modes.get(index);
    }

    /**
     * 检查是否为指定模式
     */
    public boolean is(String mode) {
        return value.equals(mode);
    }
}