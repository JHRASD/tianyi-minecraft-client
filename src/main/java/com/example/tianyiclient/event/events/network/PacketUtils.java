package com.example.tianyiclient.event.events.network;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.c2s.play.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 网络包实用工具类
 */
public class PacketUtils {
    // 日志控制
    private static boolean silentMode = true; // 默认静默模式，防止控制台爆炸

    // 重要包集合（需要记录日志的包）
    private static final Set<Class<?>> IMPORTANT_PACKETS = new HashSet<>();

    static {
        try {
            // === 客户端→服务器的重要包 ===

            // 1. 移动相关包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket");

            // 2. 交互相关包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket");

            // 3. 聊天和命令包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket");

            // 4. 玩家状态包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.HandSwingC2SPacket");

            // === 服务器→客户端的重要包 ===

            // 1. 实体相关包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket");

            // 2. 玩家相关包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket");

            // 3. 世界相关包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.ExplosionS2CPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket");

            // 4. 聊天和系统包
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.GameMessageS2CPacket");

            // 5. KeepAlive包（重要）
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket");
            addIfExists(IMPORTANT_PACKETS, "net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket");

        } catch (Exception e) {
            debugLog("初始化包列表时出错: " + e.getMessage());
        }
    }

    /**
     * 安全地添加类到集合
     */
    private static void addIfExists(Set<Class<?>> set, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            set.add(clazz);
        } catch (ClassNotFoundException e) {
            // 忽略不存在的类
        }
    }

    /**
     * 设置静默模式
     * @param silent true=静默模式（不打印日志），false=调试模式
     */
    public static void setSilentMode(boolean silent) {
        silentMode = silent;
        debugLog("静默模式设置为: " + silent);
    }

    /**
     * 检查是否为重要包（需要特别关注的包）
     */
    public static boolean isImportantPacket(Packet<?> packet) {
        if (packet == null) return false;

        // 方法1：使用预定义的类集合
        for (Class<?> clazz : IMPORTANT_PACKETS) {
            if (clazz.isInstance(packet)) {
                return true;
            }
        }

        // 方法2：根据类名判断（备用）
        String className = packet.getClass().getName().toLowerCase();
        return className.contains("move") ||
                className.contains("interact") ||
                className.contains("chat") ||
                className.contains("entity") ||
                className.contains("player");
    }

    /**
     * 安全调试日志（仅在非静默模式时打印）
     */
    public static void debugLog(String message) {
        if (!silentMode) {
            System.out.println("[PacketUtils] " + message);
        }
    }

    /**
     * 获取包的简单类名
     */
    public static String getSimpleName(Packet<?> packet) {
        if (packet == null) return "null";
        String fullName = packet.getClass().getSimpleName();

        // 常见包名简化
        if (fullName.contains("$")) {
            // 处理内部类
            fullName = fullName.substring(fullName.lastIndexOf('$') + 1);
        }

        // 移除常见后缀
        if (fullName.endsWith("S2CPacket")) {
            return fullName.substring(0, fullName.length() - 9);
        }
        if (fullName.endsWith("C2SPacket")) {
            return fullName.substring(0, fullName.length() - 9);
        }
        if (fullName.endsWith("Packet")) {
            return fullName.substring(0, fullName.length() - 6);
        }

        return fullName;
    }

    /**
     * 检查是否为移动包
     */
    public static boolean isMovementPacket(Packet<?> packet) {
        if (packet == null) return false;
        String className = packet.getClass().getName().toLowerCase();
        return className.contains("move") || className.contains("input");
    }

    /**
     * 检查是否为实体包
     */
    public static boolean isEntityPacket(Packet<?> packet) {
        if (packet == null) return false;
        String className = packet.getClass().getName().toLowerCase();
        return className.contains("entity");
    }

    /**
     * 检查是否为聊天包
     */
    public static boolean isChatPacket(Packet<?> packet) {
        if (packet == null) return false;
        String className = packet.getClass().getName().toLowerCase();
        return className.contains("chat") || className.contains("message");
    }

    /**
     * 检查是否为KeepAlive包
     */
    public static boolean isKeepAlivePacket(Packet<?> packet) {
        if (packet == null) return false;
        String className = packet.getClass().getName().toLowerCase();
        return className.contains("keepalive");
    }

    /**
     * 获取静默模式状态
     */
    public static boolean isSilentMode() {
        return silentMode;
    }
}