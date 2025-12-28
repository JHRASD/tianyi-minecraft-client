package com.example.tianyiclient.network;

import net.minecraft.network.packet.Packet;

/**
 * Packet包装器 - 将Minecraft的Packet包装成统一格式
 * 便于我们拦截、修改、注入操作
 */
public class PacketWrapper {
    private final Packet<?> rawPacket;  // 原始的Minecraft数据包
    private final long timestamp;       // 时间戳（用于节流控制）
    private final Direction direction;  // 数据包方向（发送/接收）
    private boolean cancelled;          // 是否取消发送/接收

    /**
     * 数据包方向枚举
     */
    public enum Direction {
        SEND,       // 客户端发送给服务器
        RECEIVE     // 服务器发送给客户端
    }

    /**
     * 构造函数
     * @param rawPacket 原始数据包
     * @param direction 数据包方向
     */
    public PacketWrapper(Packet<?> rawPacket, Direction direction) {
        this.rawPacket = rawPacket;
        this.direction = direction;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
    }

    /**
     * 获取原始数据包
     */
    public Packet<?> getRawPacket() {
        return rawPacket;
    }

    /**
     * 获取数据包类名（用于识别包类型）
     */
    public String getPacketName() {
        return rawPacket.getClass().getSimpleName();
    }

    /**
     * 获取完整类名
     */
    public String getFullPacketName() {
        return rawPacket.getClass().getName();
    }

    /**
     * 获取数据包方向
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 获取时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 检查是否已取消
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 设置取消状态
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * 取消这个数据包
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * 检查是否是特定类型的包
     */
    public boolean isInstanceOf(Class<?> packetClass) {
        return packetClass.isInstance(rawPacket);
    }

    /**
     * 将数据包转换为特定类型
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet<?>> T getAs(Class<T> packetClass) {
        if (isInstanceOf(packetClass)) {
            return (T) rawPacket;
        }
        return null;
    }

    /**
     * 判断是否是玩家位置包（常用于飞行、移动相关功能）
     * 根据你的Mixin，玩家移动包可能在 ClientPlayerEntityMixin.onSendMovementPacketsHead 中处理
     */
    public boolean isPlayerPositionPacket() {
        String name = getPacketName();
        // 根据你的Mixin和实际版本调整
        return name.contains("MoveC2SPacket") ||
                name.contains("PlayerMove") ||
                name.contains("VehicleMove") ||
                name.contains("Position") ||
                name.contains("Look");
    }

    /**
     * 判断是否是玩家交互包（攻击、放置方块等）
     * 根据你的 ClientPlayerInteractionManagerMixin 可进一步细化
     */
    public boolean isPlayerActionPacket() {
        String name = getPacketName();
        return name.contains("PlayerInteract") ||
                name.contains("PlayerAction") ||
                name.contains("Attack") ||
                name.contains("UseItem");
    }

    @Override
    public String toString() {
        return String.format("PacketWrapper{name=%s, direction=%s, cancelled=%s}",
                getPacketName(), direction, cancelled);
    }
}