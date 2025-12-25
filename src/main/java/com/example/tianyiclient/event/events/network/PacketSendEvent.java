package com.example.tianyiclient.event.events.network;

import net.minecraft.network.packet.Packet;

/**
 * 发送数据包事件
 */
public class PacketSendEvent extends PacketEvent {
    public PacketSendEvent(Packet<?> packet) {
        super(packet);
    }

    @Override
    public String toString() {
        return String.format("PacketSendEvent{packet=%s}", getPacketClassName());
    }
}