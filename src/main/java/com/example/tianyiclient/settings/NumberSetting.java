// NumberSetting.java
package com.example.tianyiclient.settings;

public class NumberSetting extends Setting<Number> {
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, String description, double defaultValue, double min, double max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = 0.01; // 默认增量
    }

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getDoubleValue() {
        return getValue().doubleValue();
    }

    public float getFloatValue() {
        return getValue().floatValue();
    }

    public int getIntValue() {
        return getValue().intValue();
    }

    public void setDoubleValue(double value) {
        // 限制在最小值和最大值之间
        value = Math.max(min, Math.min(max, value));
        setValue(value);
    }

    public void setFloatValue(float value) {
        setDoubleValue(value);
    }

    public void setIntValue(int value) {
        setDoubleValue(value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }
}