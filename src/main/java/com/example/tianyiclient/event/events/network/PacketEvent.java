package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.network.packet.Packet;

/**
 * 网络包事件基类
 */
public abstract class PacketEvent extends Event implements Cancelable {
    protected final Packet<?> packet;
    protected boolean cancelled = false;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * 获取数据包类名
     */
    public String getPacketClassName() {
        return packet.getClass().getSimpleName();
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