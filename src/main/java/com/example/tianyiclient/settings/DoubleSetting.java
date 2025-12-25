package com.example.tianyiclient.settings;

public class DoubleSetting extends Setting<Double> {

    private final double min;
    private final double max;

    public DoubleSetting(String name, String description, double defaultValue, double min, double max) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        clamp();
    }

    public double getValueDouble() {
        return getValue() != null ? getValue() : min;
    }

    public void setValueDouble(double value) {
        setValue(clampValue(value));
    }

    private double clampValue(double value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private void clamp() {
        setValue(clampValue(getValueDouble()));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
