package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.network.packet.Packet;

/**
 * 网络包事件基类 - 支持两种使用方式：
 * 1. 直接使用原始Packet<?>（兼容现有代码）
 * 2. 使用PacketWrapper（新系统）
 */
public abstract class PacketEvent extends Event implements Cancelable {
    private Packet<?> packet;           // 原始数据包
    private boolean cancelled = false;
    private final long timestamp;
    private com.example.tianyiclient.network.PacketWrapper wrapper;  // 新增：PacketWrapper引用

    // 构造函数1：接收原始Packet（保持向后兼容）
    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
        this.timestamp = System.currentTimeMillis();
        this.wrapper = null;  // 稍后延迟创建
    }

    // 构造函数2：接收PacketWrapper（新系统）
    public PacketEvent(com.example.tianyiclient.network.PacketWrapper wrapper) {
        this.wrapper = wrapper;
        this.packet = wrapper.getRawPacket();
        this.timestamp = wrapper.getTimestamp();
    }

    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * 获取PacketWrapper（如果不存在则创建）
     */
    public com.example.tianyiclient.network.PacketWrapper getPacketWrapper() {
        if (wrapper == null && packet != null) {
            // 根据事件类型推断方向
            com.example.tianyiclient.network.PacketWrapper.Direction direction;
            if (this instanceof PacketSendEvent) {
                direction = com.example.tianyiclient.network.PacketWrapper.Direction.SEND;
            } else {
                direction = com.example.tianyiclient.network.PacketWrapper.Direction.RECEIVE;
            }
            wrapper = new com.example.tianyiclient.network.PacketWrapper(packet, direction);
        }
        return wrapper;
    }

    /**
     * 设置新的数据包
     */
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
        this.wrapper = null;  // 重置wrapper，因为packet变了
    }

    /**
     * 设置新的PacketWrapper
     */
    public void setPacketWrapper(com.example.tianyiclient.network.PacketWrapper wrapper) {
        this.wrapper = wrapper;
        this.packet = wrapper.getRawPacket();
    }

    /**
     * 获取特定类型的包（类型安全的转换）
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet<?>> T getPacket(Class<T> type) {
        return (T) packet;
    }

    /**
     * 获取数据包类名
     */
    public String getPacketClassName() {
        return packet.getClass().getSimpleName();
    }

    /**
     * 获取包名（不含完整路径）
     */
    public String getSimplePacketName() {
        String fullName = packet.getClass().getSimpleName();
        // 移除Packet后缀（如果有）
        if (fullName.endsWith("S2CPacket")) {
            return fullName.substring(0, fullName.length() - 9);
        } else if (fullName.endsWith("C2SPacket")) {
            return fullName.substring(0, fullName.length() - 9);
        }
        return fullName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}