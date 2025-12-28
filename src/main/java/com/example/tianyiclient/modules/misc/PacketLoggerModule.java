package com.example.tianyiclient.modules.misc;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.network.PacketEvent;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.*;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.Queue;

public class PacketLoggerModule extends Module {

    // 设置
    private final BoolSetting logInbound;
    private final BoolSetting logOutbound;
    private final BoolSetting logImportantOnly;
    private final IntegerSetting maxLogCount;
    private final BoolSetting showInChat;
    private final BoolSetting showInConsole;

    // 统计
    private int totalPackets = 0;
    private int inboundPackets = 0;
    private int outboundPackets = 0;
    private int cancelledPackets = 0;

    // 日志队列
    private final Queue<String> recentLogs = new LinkedList<>();
    private static final int MAX_RECENT_LOGS = 100;

    public PacketLoggerModule() {
        super("PacketLogger", "记录和显示网络数据包", Category.其他);

        // 默认快捷键
        setKeybind(GLFW.GLFW_KEY_P);

        // 创建设置
        SettingGroup settings = new SettingGroup();

        logInbound = new BoolSetting("记录接收包", "记录从服务器接收的数据包", true);
        logOutbound = new BoolSetting("记录发送包", "记录发送到服务器的数据包", true);
        logImportantOnly = new BoolSetting("仅重要包", "只记录重要的数据包", false);
        maxLogCount = new IntegerSetting("最大显示", "最多显示多少条日志", 50, 10, 200);
        showInChat = new BoolSetting("聊天显示", "在聊天栏显示重要包", true);
        showInConsole = new BoolSetting("控制台显示", "在控制台显示所有包", true);

        settings.add(logInbound);
        settings.add(logOutbound);
        settings.add(logImportantOnly);
        settings.add(maxLogCount);
        settings.add(showInChat);
        settings.add(showInConsole);

        // 添加到模块设置
        for (Setting<?> setting : settings.getSettings()) {
            addSetting(setting);
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        System.out.println("[PacketLogger] 模块已启用");
        resetStats();
        setDisplayInfo("已启用");
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        System.out.println("[PacketLogger] 模块已禁用");
        System.out.println("[PacketLogger] 统计: " + getStats());
        setDisplayInfo(null);
    }

    @EventHandler(priority = Priority.LOW)
    public void onPacketEvent(PacketEvent event) {
        if (!isEnabled()) return;

        // 更新统计
        totalPackets++;

        if (event instanceof com.example.tianyiclient.event.events.network.PacketReceiveEvent) {
            inboundPackets++;
            System.out.println("[PacketLogger] 收到接收包 #" + inboundPackets + ": " +
                    event.getPacket().getClass().getSimpleName());
        } else {
            outboundPackets++;
            System.out.println("[PacketLogger] 收到发送包 #" + outboundPackets + ": " +
                    event.getPacket().getClass().getSimpleName());
        }

        if (event.isCancelled()) {
            cancelledPackets++;
            System.out.println("[PacketLogger] 包被取消: " + event.getPacket().getClass().getSimpleName());
        }

        // 更新显示信息
        updateDisplayInfo();

        // 记录日志
        if (shouldLog(event)) {
            logPacket(event);
        }
    }

    /**
     * 判断是否应该记录这个包
     */
    private boolean shouldLog(PacketEvent event) {
        // 检查方向
        boolean isInbound = event instanceof com.example.tianyiclient.event.events.network.PacketReceiveEvent;

        if (isInbound && !logInbound.isEnabled()) {
            return false;
        }

        if (!isInbound && !logOutbound.isEnabled()) {
            return false;
        }

        return true;
    }

    /**
     * 记录数据包
     */
    private void logPacket(PacketEvent event) {
        boolean isInbound = event instanceof com.example.tianyiclient.event.events.network.PacketReceiveEvent;
        String direction = isInbound ? "← 接收" : "→ 发送";
        String packetName = event.getPacket().getClass().getSimpleName();
        String status = event.isCancelled() ? " [已取消]" : "";

        String fullLog = direction + " " + packetName + status;

        // 添加到最近日志
        addToRecentLogs(fullLog);

        // 控制台输出
        if (showInConsole.isEnabled()) {
            System.out.println("[PacketLogger] " + fullLog);
        }
    }

    /**
     * 添加到最近日志队列
     */
    private void addToRecentLogs(String log) {
        recentLogs.add(log);
        while (recentLogs.size() > maxLogCount.getValue()) {
            recentLogs.poll();
        }
    }

    /**
     * 更新显示信息
     */
    private void updateDisplayInfo() {
        setDisplayInfo(String.format("§7In:§f%d §7Out:§f%d §7Blocked:§f%d",
                inboundPackets, outboundPackets, cancelledPackets));
    }

    /**
     * 重置统计
     */
    public void resetStats() {
        totalPackets = 0;
        inboundPackets = 0;
        outboundPackets = 0;
        cancelledPackets = 0;
        recentLogs.clear();
        updateDisplayInfo();
    }

    /**
     * 获取统计信息
     */
    public String getStats() {
        return String.format("总计: %d, 接收: %d, 发送: %d, 阻止: %d",
                totalPackets, inboundPackets, outboundPackets, cancelledPackets);
    }

    /**
     * 获取最近的日志
     */
    public Queue<String> getRecentLogs() {
        return new LinkedList<>(recentLogs);
    }
}