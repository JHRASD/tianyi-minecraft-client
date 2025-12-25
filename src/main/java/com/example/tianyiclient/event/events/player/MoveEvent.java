package com.example.tianyiclient.event.events.player;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.util.math.Vec3d;

/**
 * 玩家移动事件
 * 当玩家位置发生变化时触发
 */
public class MoveEvent extends Event implements Cancelable {
    private final Vec3d from;
    private final Vec3d to;
    private boolean cancelled = false;

    public MoveEvent(Vec3d from, Vec3d to) {
        this.from = from;
        this.to = to;
    }

    public Vec3d getFrom() {
        return from;
    }

    public Vec3d getTo() {
        return to;
    }

    /**
     * 获取移动向量
     */
    public Vec3d getDelta() {
        return to.subtract(from);
    }

    /**
     * 获取X轴移动距离
     */
    public double getDeltaX() {
        return to.x - from.x;
    }

    /**
     * 获取Y轴移动距离
     */
    public double getDeltaY() {
        return to.y - from.y;
    }

    /**
     * 获取Z轴移动距离
     */
    public double getDeltaZ() {
        return to.z - from.z;
    }

    /**
     * 获取水平移动距离
     */
    public double getHorizontalDistance() {
        double dx = getDeltaX();
        double dz = getDeltaZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * 获取总移动距离
     */
    public double getTotalDistance() {
        return from.distanceTo(to);
    }

    /**
     * 检查是否为垂直移动（主要上下移动）
     */
    public boolean isVerticalMove() {
        return Math.abs(getDeltaY()) > Math.abs(getDeltaX()) &&
                Math.abs(getDeltaY()) > Math.abs(getDeltaZ());
    }

    /**
     * 检查是否为水平移动（主要水平移动）
     */
    public boolean isHorizontalMove() {
        return !isVerticalMove();
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
        return String.format("MoveEvent{from=[%.2f,%.2f,%.2f], to=[%.2f,%.2f,%.2f], distance=%.2f}",
                from.x, from.y, from.z, to.x, to.y, to.z, getTotalDistance());
    }
}