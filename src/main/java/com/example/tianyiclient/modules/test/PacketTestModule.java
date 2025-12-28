package com.example.tianyiclient.modules.test;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.network.PacketSendEvent;
import com.example.tianyiclient.event.events.network.PacketReceiveEvent;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.BoolSetting;
import com.example.tianyiclient.settings.IntegerSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Packet系统测试模块
 * 用于验证Packet拦截、修改、取消功能
 */
public class PacketTestModule extends Module {

    // 设置项
    private final BoolSetting logSentPackets;
    private final BoolSetting logReceivedPackets;
    private final BoolSetting cancelMovementPackets;
    private final BoolSetting showMessages;
    private final IntegerSetting cancelChance;

    // 计数器
    private int sentPacketCount = 0;
    private int receivedPacketCount = 0;
    private int cancelledPacketCount = 0;

    // Minecraft客户端实例
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public PacketTestModule() {
        super("Packet测试", "测试Packet系统功能", Category.其他);

        System.out.println("[PacketTestModule] 构造函数调用，类名: " + this.getClass().getName());

        // BoolSetting - 根据你的构造函数
        logSentPackets = new BoolSetting("记录发送包", "记录所有发送的数据包", false);
        logReceivedPackets = new BoolSetting("记录接收包", "记录所有接收的数据包", false);
        cancelMovementPackets = new BoolSetting("取消移动包", "随机取消移动包", false);
        showMessages = new BoolSetting("显示消息", "显示包操作消息", true);

        // IntegerSetting - 根据你提供的构造函数
        cancelChance = new IntegerSetting("取消几率%", "取消包的几率百分比", 0, 0, 100);

        // 添加设置
        addSetting(logSentPackets);
        addSetting(logReceivedPackets);
        addSetting(cancelMovementPackets);
        addSetting(showMessages);
        addSetting(cancelChance);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        setDisplayInfo("已启用");
        resetCounters();

        // 发送启用消息
        if (mc.player != null && showMessages.getValue()) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§a[Packet测试] 模块已启用"), false);
        }

        // 控制台输出
        System.out.println("[Packet测试] 模块已启用");
        System.out.println("[Packet测试] 取消几率: " + cancelChance.getValue() + "%");
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        setDisplayInfo(null);

        // 发送统计信息
        if (mc.player != null && showMessages.getValue()) {
            mc.player.sendMessage(net.minecraft.text.Text.literal(
                    String.format("§6[Packet测试] 统计: §f发送=%d, 接收=%d, 取消=%d",
                            sentPacketCount, receivedPacketCount, cancelledPacketCount)
            ), false);
        }

        // 控制台输出统计
        System.out.printf("[Packet测试] 统计: 发送包=%d, 接收包=%d, 取消包=%d%n",
                sentPacketCount, receivedPacketCount, cancelledPacketCount);
    }

    /**
     * 监听发送的包 - 修正为具体事件类型
     */
    @EventHandler(priority = Priority.HIGHEST)
    public void onPacketSend(PacketSendEvent event) {
        if (!isEnabled()) {
            return;
        }

        sentPacketCount++;

        String packetName = event.getPacketClassName();

        // 记录包信息到控制台
        if (logSentPackets.getValue()) {
            System.out.printf("[Packet测试] 发送包: %s%n", packetName);
        }

        // 检查是否是移动包
        boolean isMovementPacket = isMovementPacket(packetName);

        // 如果启用取消移动包且是移动包，则根据几率取消
        if (cancelMovementPackets.getValue() && isMovementPacket) {
            // 根据几率决定是否取消
            if (shouldCancelPacket()) {
                event.setCancelled(true);
                cancelledPacketCount++;

                // 显示取消消息
                if (mc.player != null && showMessages.getValue()) {
                    mc.player.sendMessage(
                            net.minecraft.text.Text.literal(
                                    String.format("§c[Packet测试] 已取消移动包: §f%s (几率: %d%%)",
                                            packetName, cancelChance.getValue())
                            ),
                            false
                    );
                }

                // 控制台输出
                if (logSentPackets.getValue()) {
                    System.out.printf("[Packet测试] 已取消移动包: %s (几率: %d%%)%n",
                            packetName, cancelChance.getValue());
                }
            }
        }
    }

    /**
     * 监听接收的包 - 修正为具体事件类型
     */
    @EventHandler(priority = Priority.HIGHEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) {
            return;
        }

        receivedPacketCount++;

        String packetName = event.getPacketClassName();

        // 记录包信息到控制台
        if (logReceivedPackets.getValue()) {
            System.out.printf("[Packet测试] 接收包: %s%n", packetName);
        }

        // 根据几率取消接收包（测试用）
        if (shouldCancelPacket()) {
            event.setCancelled(true);
            cancelledPacketCount++;

            if (mc.player != null && showMessages.getValue()) {
                mc.player.sendMessage(
                        net.minecraft.text.Text.literal(
                                String.format("§c[Packet测试] 已取消接收包: §f%s (几率: %d%%)",
                                        packetName, cancelChance.getValue())
                        ),
                        false
                );
            }

            if (logReceivedPackets.getValue()) {
                System.out.printf("[Packet测试] 已取消接收包: %s (几率: %d%%)%n",
                        packetName, cancelChance.getValue());
            }
        }
    }

    /**
     * 判断是否是移动包
     */
    private boolean isMovementPacket(String packetName) {
        return packetName.contains("Move") ||
                packetName.contains("Position") ||
                packetName.contains("Look") ||
                packetName.contains("Vehicle") ||
                packetName.contains("Input");
    }

    /**
     * 根据几率判断是否应该取消包
     */
    private boolean shouldCancelPacket() {
        if (cancelChance.getValue() <= 0) {
            return false;
        }
        if (cancelChance.getValue() >= 100) {
            return true;
        }

        // 生成0-99的随机数，如果小于取消几率则取消
        int random = (int) (Math.random() * 100);
        return random < cancelChance.getValue();
    }

    /**
     * 重置计数器
     */
    private void resetCounters() {
        sentPacketCount = 0;
        receivedPacketCount = 0;
        cancelledPacketCount = 0;
    }

    /**
     * 获取当前统计信息
     */
    public String getCurrentStats() {
        return String.format("§6发送: §f%d §6接收: §f%d §6取消: §f%d",
                sentPacketCount, receivedPacketCount, cancelledPacketCount);
    }
}