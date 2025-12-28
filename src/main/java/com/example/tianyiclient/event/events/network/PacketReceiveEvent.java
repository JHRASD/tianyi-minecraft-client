package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.network.PacketWrapper;
import net.minecraft.network.packet.Packet;

/**
 * 接收数据包事件（服务器→客户端）
 */
public class PacketReceiveEvent extends PacketEvent {
    // 构造函数1：接收原始Packet（兼容现有代码）
    public PacketReceiveEvent(Packet<?> packet) {
        super(packet);
    }

    // 构造函数2：接收PacketWrapper（新系统）
    public PacketReceiveEvent(PacketWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String toString() {
        return String.format("PacketReceiveEvent{packet=%s, cancelled=%s}",
                getPacketClassName(), isCancelled());
    }
}