package com.example.tianyiclient.settings;

public class NumberSetting extends Setting<Double> { // 指定泛型参数为 Double
    private final double min;
    private final double max;
    private final double increment;

    // 修正构造函数：符合父类Setting的3个参数
    public NumberSetting(String name, String id, double defaultValue,
                         double min, double max, double increment) {
        super(name, id, defaultValue); // 现在有3个参数
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    // 重写父类的getValue，返回Double（兼容）
    @Override
    public Double getValue() {
        return super.getValue(); // 从父类获取
    }

    // 重写父类的setValue
    @Override
    public void setValue(Double value) {
        double rounded = Math.round(value / increment) * increment;
        double clamped = Math.max(min, Math.min(max, rounded));
        super.setValue(clamped);
    }

    // 为了方便，保留设置double值的方法
    public void setValue(double value) {
        setValue(Double.valueOf(value));
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getIncrement() { return increment; }
}