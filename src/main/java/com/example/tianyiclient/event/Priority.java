package com.example.tianyiclient.event;

import java.util.Comparator;

/**
 * 事件监听器的优先级
 * 优先级高的监听器先执行，同优先级按注册顺序执行
 */
public enum Priority {
    /**
     * 最高优先级 - 最先执行，用于系统级处理
     */
    HIGHEST(100),

    /**
     * 高优先级 - 较早执行，用于修改或预处理事件
     */
    HIGH(75),

    /**
     * 普通优先级 - 默认优先级
     */
    NORMAL(50),

    /**
     * 低优先级 - 较晚执行，用于观察或记录事件
     */
    LOW(25),

    /**
     * 最低优先级 - 最后执行，用于最终处理或清理
     */
    LOWEST(0);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    /**
     * 获取优先级的数值表示
     * @return 优先级数值
     */
    public int getValue() {
        return value;
    }

    /**
     * 比较两个优先级
     * @param other 另一个优先级
     * @return 如果当前优先级更高返回true
     */
    public boolean isHigherThan(Priority other) {
        return this.value > other.value;
    }

    /**
     * 比较两个优先级
     * @param other 另一个优先级
     * @return 如果当前优先级更低返回true
     */
    public boolean isLowerThan(Priority other) {
        return this.value < other.value;
    }

    /**
     * 获取优先级比较器（从高到低排序）
     * @return 优先级比较器
     */
    public static Comparator<Priority> comparator() {
        return Comparator.comparingInt(Priority::getValue).reversed();
    }

    /**
     * 根据数值获取优先级
     * @param value 优先级数值
     * @return 对应的优先级，如果没有匹配则返回NORMAL
     */
    public static Priority fromValue(int value) {
        for (Priority priority : values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        return NORMAL;
    }

    /**
     * 获取下一个更低的优先级
     * @return 更低的优先级，如果已经是最低则返回自身
     */
    public Priority nextLower() {
        int nextValue = Math.max(0, this.value - 25);
        return fromValue(nextValue);
    }

    /**
     * 获取下一个更高的优先级
     * @return 更高的优先级，如果已经是最高则返回自身
     */
    public Priority nextHigher() {
        int nextValue = Math.min(100, this.value + 25);
        return fromValue(nextValue);
    }
}