package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.event.EventBus;
import net.minecraft.network.packet.Packet;

/**
 * 数据包事件辅助类。
 * 作为公共桥接器，允许Mixin等外部代码访问包级私有的 PacketReceiveEvent 和 PacketSendEvent。
 */
public class PacketEventHelper {

    /**
     * 处理数据包接收事件。
     * @param packet 接收到的数据包
     * @return 事件是否被取消
     */
    public static boolean handleReceiveEvent(Packet<?> packet) {
        // 1. 创建事件实例（调用包内私有类的构造函数）
        PacketEvent event = new PacketReceiveEvent(packet);
        // 2. 通过事件总线发布事件
        EventBus.getInstance().post(event);
        // 3. 返回事件是否被取消（假设基类PacketEvent有isCancelled方法）
        // 如果事件类没有isCancelled()，请替换为实际的判断逻辑，例如检查event.cancelled字段
        return event.isCancelled();
    }

    /**
     * 处理数据包发送事件。
     * @param packet 要发送的数据包
     * @return 事件是否被取消
     */
    public static boolean handleSendEvent(Packet<?> packet) {
        // 逻辑同上，但创建发送事件
        PacketEvent event = new PacketSendEvent(packet);
        EventBus.getInstance().post(event);
        return event.isCancelled();
    }
}