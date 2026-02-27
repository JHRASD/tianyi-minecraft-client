package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.network.packet.Packet;

/**
 * 网络包事件 - 发送和接收
 */
public class PacketEvent extends Event implements Cancelable {
    private final Packet<?> packet;
    private final Direction direction;
    private boolean cancelled = false;

    public enum Direction {
        SEND,   // 客户端发送给服务器
        RECEIVE // 客户端从服务器接收
    }

    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isSend() {
        return direction == Direction.SEND;
    }

    public boolean isReceive() {
        return direction == Direction.RECEIVE;
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