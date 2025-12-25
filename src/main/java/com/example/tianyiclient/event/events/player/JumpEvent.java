package com.example.tianyiclient.event.events.player;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;

/**
 * 玩家跳跃事件
 * 当玩家执行跳跃动作时触发
 */
public class JumpEvent extends Event implements Cancelable {
    private final double jumpStrength;
    private boolean cancelled = false;

    public JumpEvent(double jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public double getJumpStrength() {
        return jumpStrength;
    }

    /**
     * 获取跳跃高度估计（基于跳跃强度）
     */
    public double getEstimatedJumpHeight() {
        // 简化的跳跃高度计算公式
        return jumpStrength * 1.5;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return String.format("JumpEvent{strength=%.2f}", jumpStrength);
    }
}