package com.example.tianyiclient.network.modifiers;

import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.PacketModifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

import java.util.concurrent.*;

/**
 * 延迟发送修改器
 * 将包延迟指定毫秒数后发送
 */
public class DelayModifier implements PacketModifier {

    private final long delayMillis;
    private final String name;
    private final MinecraftClient mc;
    private final ScheduledExecutorService scheduler;
    private final PacketEngine packetEngine;

    // 用于存储延迟任务的Map，便于取消
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> delayedTasks;

    public DelayModifier(long delayMillis) {
        this.delayMillis = Math.max(0, delayMillis); // 确保非负
        this.name = "延迟发送修改器[" + delayMillis + "ms]";
        this.mc = MinecraftClient.getInstance();
        this.packetEngine = PacketEngine.getInstance();

        // 创建单线程调度器（避免创建太多线程）
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "Packet-Delay-Worker");
            thread.setDaemon(true); // 守护线程
            return thread;
        });

        this.delayedTasks = new ConcurrentHashMap<>();
    }

    @Override
    public Packet<?> modify(Packet<?> original) {
        if (original == null) {
            return null;
        }

        if (delayMillis <= 0) {
            return original; // 不延迟，直接返回原包
        }

        System.out.println("[DelayModifier] ⏰ 延迟 " + delayMillis + "ms 发送: " + original.getClass().getSimpleName());

        // 使用包哈希码作为任务ID
        int packetId = System.identityHashCode(original);

        // 安排延迟发送任务
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                // 延迟完成后发送包
                sendPacketDirectly(original);
                System.out.println("[DelayModifier] ✅ 延迟发送完成: " + original.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("[DelayModifier] ❌ 延迟发送失败: " + e.getMessage());
            } finally {
                // 从任务Map中移除
                delayedTasks.remove(packetId);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);

        // 保存任务引用（可用于取消）
        delayedTasks.put(packetId, future);

        // 返回null告诉PacketEngine不要立即发送
        return null;
    }

    /**
     * 直接发送包（绕过PacketEngine避免递归）
     */
    private void sendPacketDirectly(Packet<?> packet) {
        if (packet == null || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            // 使用安全的发送方式
            ClientConnection connection = mc.getNetworkHandler().getConnection();
            if (connection != null) {
                // 直接通过ClientConnection发送，绕过拦截
                connection.send(packet);

                // 在聊天中显示提示（调试用）
                if (mc.player != null) {
                    String packetName = packet.getClass().getSimpleName();
                    if (packetName.length() > 20) {
                        packetName = packetName.substring(0, 20) + "...";
                    }
                    mc.player.sendMessage(
                            Text.literal("§8[延迟发送] §7" + packetName + " §8(" + delayMillis + "ms)"),
                            false
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[DelayModifier] ❌ 直接发送失败: " + e.getMessage());

            // 降级：尝试通过PacketEngine发送
            packetEngine.sendPacketSafely(packet);
        }
    }

    /**
     * 取消所有延迟中的包（模块禁用时调用）
     */
    public void cancelAllDelayedPackets() {
        System.out.println("[DelayModifier] 取消所有延迟中的包 (" + delayedTasks.size() + " 个)");

        int cancelledCount = 0;
        for (ScheduledFuture<?> future : delayedTasks.values()) {
            if (future != null && !future.isDone()) {
                if (future.cancel(false)) {
                    cancelledCount++;
                }
            }
        }

        delayedTasks.clear();

        if (cancelledCount > 0) {
            System.out.println("[DelayModifier] ✅ 已取消 " + cancelledCount + " 个延迟包");

            // 通知玩家（调试用）
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§c已取消 " + cancelledCount + " 个延迟包"), false);
            }
        }
    }

    /**
     * 关闭调度器（清理资源）
     */
    public void shutdown() {
        cancelAllDelayedPackets();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("[DelayModifier] 调度器已关闭");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ModifierType getType() {
        return ModifierType.DELAY;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * 获取当前延迟中的包数量
     */
    public int getDelayedPacketCount() {
        int count = 0;
        for (ScheduledFuture<?> future : delayedTasks.values()) {
            if (future != null && !future.isDone()) {
                count++;
            }
        }
        return count;
    }
}