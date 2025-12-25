package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Event;

/**
 * Tick事件 - 每游戏tick触发一次
 */
public class TickEvent extends Event {
    private final Phase phase;
    private final long tickCount; // 新增字段
    private final float partialTicks; // 新增字段

    // 三参数构造函数（匹配 ClientEvents.java 的调用）
    public TickEvent(Phase phase, long tickCount, float partialTicks) {
        this.phase = phase;
        this.tickCount = tickCount;
        this.partialTicks = partialTicks;
    }

    // 可选：保留单参数构造函数（如果你其他地方用到）
    public TickEvent(Phase phase) {
        this(phase, 0, 0.0f); // 调用三参数构造函数，使用默认值
    }

    public Phase getPhase() {
        return phase;
    }

    public long getTickCount() {
        return tickCount;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * Tick阶段枚举
     */
    public enum Phase {
        /** 客户端开始tick时 */
        START,
        /** 客户端结束tick时 */
        END
    }
}