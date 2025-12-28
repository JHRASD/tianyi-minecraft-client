package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.network.PacketWrapper;
import net.minecraft.network.packet.Packet;

/**
 * 发送数据包事件（客户端→服务器）
 */
public class PacketSendEvent extends PacketEvent {
    // 构造函数1：接收原始Packet（兼容现有代码）
    public PacketSendEvent(Packet<?> packet) {
        super(packet);
    }

    // 构造函数2：接收PacketWrapper（新系统）
    public PacketSendEvent(PacketWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String toString() {
        return String.format("PacketSendEvent{packet=%s, cancelled=%s}",
                getPacketClassName(), isCancelled());
    }
}