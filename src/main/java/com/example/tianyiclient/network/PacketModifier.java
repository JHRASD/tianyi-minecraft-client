package com.example.tianyiclient.network;

import net.minecraft.network.packet.Packet;

/**
 * 数据包修改器接口
 * 所有包修改器都必须实现此接口
 */
public interface PacketModifier {

    /**
     * 修改数据包
     * @param original 原始数据包
     * @return 修改后的数据包，如果返回null则使用原包
     */
    Packet<?> modify(Packet<?> original);

    /**
     * 获取修改器名称（用于调试）
     */
    String getName();

    /**
     * 获取修改器类型
     */
    ModifierType getType();

    /**
     * 修改器类型枚举
     */
    enum ModifierType {
        /** 重定向攻击目标 */
        REDIRECT_ATTACK,
        /** 延迟发送 */
        DELAY,
        /** 添加额外包序列 */
        SEQUENCE,
        /** 修改包内容 */
        CONTENT,
        /** 时序调整 */
        TIMING
    }
}