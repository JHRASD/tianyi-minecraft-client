package com.example.tianyiclient.settings;

public class IntegerSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntegerSetting(String name, String description, Integer defaultValue) {
        this(name, description, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerSetting(String name, String description, Integer defaultValue, int min, int max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        // 确保默认值在范围内
        clampValue();
    }

    private void clampValue() {
        Integer currentValue = getValue();
        if (currentValue != null) {
            if (currentValue < min) {
                setValue(min);
            } else if (currentValue > max) {
                setValue(max);
            }
        }
    }

    @Override
    public void setValue(Integer value) {
        // 限制值在最小和最大值之间
        if (value < min) {
            super.setValue(min);
        } else if (value > max) {
            super.setValue(max);
        } else {
            super.setValue(value);
        }
    }

    // 添加便捷方法
    public int getValueInt() {
        return getValue() != null ? getValue() : min;
    }

    public void setValueInt(int value) {
        setValue(value);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}