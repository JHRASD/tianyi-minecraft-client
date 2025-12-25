package com.example.tianyiclient.event.events.network;

import net.minecraft.network.packet.Packet;

/**
 * 接收数据包事件
 */
public class PacketReceiveEvent extends PacketEvent {
    public PacketReceiveEvent(Packet<?> packet) {
        super(packet);
    }

    @Override
    public String toString() {
        return String.format("PacketReceiveEvent{packet=%s}", getPacketClassName());
    }
}