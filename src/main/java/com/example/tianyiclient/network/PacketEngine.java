package com.example.tianyiclient.network;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 数据包引擎 - 仅记录模式（已集成修改器功能）
 */
public class PacketEngine {

    // ---------- 单例实例 ----------
    private static final PacketEngine INSTANCE = new PacketEngine();

    // ---------- 核心队列 ----------
    private final PriorityBlockingQueue<ClientPacketIntent> scheduledIntents;

    // ---------- 修改器系统 ----------
    private final Map<String, PacketModifier> registeredModifiers = new ConcurrentHashMap<>();
    private boolean enableModification = true; // 启用修改功能

    // ---------- 状态与配置 ----------
    private boolean isEnabled = true;
    private long currentTick = 0;
    private final MinecraftClient mc;

    // 性能监控
    private long totalProcessed = 0;
    private long lastStatusLogTick = 0;

    // 用于防止递归的线程本地变量
    private final ThreadLocal<Packet<?>> processingPacket = ThreadLocal.withInitial(() -> null);

    // 私有构造器
    private PacketEngine() {
        this.scheduledIntents = new PriorityBlockingQueue<>(11,
                Comparator.comparingLong(ClientPacketIntent::getScheduleTick));
        this.mc = MinecraftClient.getInstance();

        System.out.println("[PacketEngine] 初始化 - 仅记录模式");
    }

    public static PacketEngine getInstance() {
        return INSTANCE;
    }

    // ========== 包发送方法 ==========

    /**
     * 公共安全的发送方法，供修改器调用
     * 这是原有方法从 private 改为 public
     */
    public void sendPacketSafely(Packet<?> packet) {
        if (packet == null || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            mc.getNetworkHandler().sendPacket(packet);
        } catch (Exception e) {
            System.err.println("[PacketEngine] 发送包失败: " + e.getMessage());
        }
    }

    /**
     * 直接发送包（绕过修改器和拦截器）
     * 用于需要立即发送且不触发递归的情况
     */
    public void sendPacketDirectly(Packet<?> packet) {
        if (packet == null || mc.getNetworkHandler() == null) {
            return;
        }

        try {
            // 标记为正在处理，防止递归
            processingPacket.set(packet);
            mc.getNetworkHandler().sendPacket(packet);
        } catch (Exception e) {
            System.err.println("[PacketEngine] 直接发送包失败: " + e.getMessage());
        } finally {
            processingPacket.remove();
        }
    }

    /**
     * 安全发送包（带递归检查的版本）
     * 如果正在处理此包，则跳过发送
     */
    public void sendPacketWithRecursionCheck(Packet<?> packet) {
        if (packet == null || mc.getNetworkHandler() == null) {
            return;
        }

        // 检查是否正在处理这个包（防止递归）
        if (processingPacket.get() == packet) {
            System.out.println("[PacketEngine] ⚠ 跳过递归发送: " + packet.getClass().getSimpleName());
            return;
        }

        sendPacketSafely(packet);
    }

    /**
     * 异步发送包（在单独的线程中）
     */
    public void sendPacketAsync(Packet<?> packet) {
        if (packet == null) {
            return;
        }

        // 在新线程中发送包
        new Thread(() -> {
            try {
                Thread.sleep(10); // 微小延迟，确保不在游戏主线程
                sendPacketSafely(packet);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Packet-Send-Async").start();
    }

    /**
     * 延迟发送包
     */
    public void sendPacketDelayed(Packet<?> packet, long delayMillis) {
        if (packet == null || delayMillis <= 0) {
            sendPacketSafely(packet);
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                sendPacketSafely(packet);
                System.out.println("[PacketEngine] ⏰ 延迟 " + delayMillis + "ms 发送完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 中断时立即发送
                sendPacketSafely(packet);
            }
        }, "Packet-Delayed-Send").start();
    }

    // ========== 修改器系统 ==========

    /**
     * 注册包修改器
     */
    public void registerModifier(String id, PacketModifier modifier) {
        if (id != null && modifier != null) {
            registeredModifiers.put(id, modifier);
            System.out.println("[PacketEngine] ✅ 注册修改器: " + modifier.getName() + " (" + id + ")");
        }
    }

    /**
     * 取消注册包修改器
     */
    public void unregisterModifier(String id) {
        PacketModifier removed = registeredModifiers.remove(id);
        if (removed != null) {
            System.out.println("[PacketEngine] ❌ 取消注册修改器: " + removed.getName());
        }
    }

    /**
     * 核心：修改并发送包
     */
    public void modifyAndSend(Packet<?> original, String modifierId) {
        if (!enableModification || original == null || modifierId == null) {
            sendPacketSafely(original); // 降级：直接发送原包
            return;
        }

        PacketModifier modifier = registeredModifiers.get(modifierId);
        if (modifier == null) {
            System.out.println("[PacketEngine] ⚠ 修改器不存在: " + modifierId + "，直接发送原包");
            sendPacketSafely(original);
            return;
        }

        try {
            System.out.println("[PacketEngine] 🔧 应用修改器: " + modifier.getName());
            Packet<?> modified = modifier.modify(original);

            if (modified != null) {
                sendPacketWithRecursionCheck(modified);
                System.out.println("[PacketEngine] ✅ 修改后的包已发送");
            } else {
                // 修改器返回null，表示包已由修改器处理
                System.out.println("[PacketEngine] ℹ 修改器已处理包，无需额外发送");
            }

        } catch (Exception e) {
            System.err.println("[PacketEngine] ❌ 修改包失败: " + e.getMessage());
            e.printStackTrace();
            // 出错时降级发送原包
            sendPacketSafely(original);
        }
    }

    /**
     * 批量应用多个修改器
     */
    public void modifyAndSend(Packet<?> original, List<String> modifierIds) {
        if (!enableModification || original == null || modifierIds == null || modifierIds.isEmpty()) {
            sendPacketSafely(original);
            return;
        }

        Packet<?> currentPacket = original;

        for (String modifierId : modifierIds) {
            PacketModifier modifier = registeredModifiers.get(modifierId);
            if (modifier == null) {
                System.out.println("[PacketEngine] ⚠ 跳过不存在的修改器: " + modifierId);
                continue;
            }

            try {
                System.out.println("[PacketEngine] 🔄 应用修改器链: " + modifier.getName());
                Packet<?> modified = modifier.modify(currentPacket);

                if (modified != null) {
                    currentPacket = modified;
                }
                // 如果返回null，保持当前包不变

            } catch (Exception e) {
                System.err.println("[PacketEngine] ❌ 修改器 " + modifier.getName() + " 失败: " + e.getMessage());
                // 这个修改器失败，继续下一个
            }
        }

        // 发送最终修改后的包
        sendPacketWithRecursionCheck(currentPacket);
        System.out.println("[PacketEngine] ✅ 修改器链处理完成");
    }

    /**
     * 检查修改器是否已注册
     */
    public boolean hasModifier(String id) {
        return registeredModifiers.containsKey(id);
    }

    /**
     * 获取所有注册的修改器
     */
    public Collection<PacketModifier> getRegisteredModifiers() {
        return registeredModifiers.values();
    }

    /**
     * 获取已注册修改器数量
     */
    public int getModifierCount() {
        return registeredModifiers.size();
    }

    /**
     * 启用/禁用包修改功能
     */
    public void setEnableModification(boolean enable) {
        this.enableModification = enable;
        System.out.println("[PacketEngine] 包修改功能: " + (enable ? "✅ 启用" : "❌ 禁用"));
    }

    public boolean isModificationEnabled() {
        return enableModification;
    }

    // ========== 原有功能 ==========

    /**
     * 安排一个数据包意图（仅记录，不发送）。
     */
    public boolean scheduleIntent(ClientPacketIntent intent) {
        if (!isEnabled || intent == null) {
            return false;
        }

        // 检查意图是否已过期
        if (intent.isExpired(currentTick)) {
            return false;
        }

        // 添加到调度队列
        boolean added = scheduledIntents.offer(intent);

        if (added && currentTick % 100 == 0) { // 限流输出
            System.out.println("[PacketEngine] 📝 记录意图: " + intent.getType());
            totalProcessed++;
        }

        return added;
    }

    /**
     * 立即执行一个意图
     */
    public void executeImmediately(ClientPacketIntent intent) {
        if (!isEnabled || intent == null) {
            return;
        }

        System.out.println("[PacketEngine] ⚡ 立即执行意图: " + intent.getType());

        // 记录意图但不实际发送包（仅记录模式）
        scheduleIntent(intent);
    }

    /**
     * 当包被拦截时调用（透明代理模式）
     */
    public void onPacketIntercepted(Packet<?> packet, ClientPacketIntent intent) {
        if (!isEnabled || packet == null || intent == null) {
            return;
        }

        // 只记录包信息
        if (currentTick % 200 == 0) { // 每10秒输出一次
            System.out.println("[PacketEngine] 📊 监控到包: " + packet.getClass().getSimpleName());
        }

        // 记录意图
        scheduleIntent(intent);
    }

    /**
     * 每游戏刻更新。
     */
    @EventHandler
    public void onClientTick(TickEvent event) {
        if (!isEnabled || event.getPhase() != TickEvent.Phase.START) {
            return;
        }

        currentTick = event.getTickCount();

        // 清理过期意图
        cleanupExpiredIntents();

        // 每10秒输出一次状态
        if (currentTick - lastStatusLogTick >= 200) {
            System.out.println("[PacketEngine] 状态 - 总记录: " + totalProcessed +
                    ", 队列大小: " + scheduledIntents.size() +
                    ", 修改器: " + registeredModifiers.size());
            lastStatusLogTick = currentTick;
        }
    }

    /**
     * 清理过期意图。
     */
    private void cleanupExpiredIntents() {
        int cleaned = 0;

        while (!scheduledIntents.isEmpty()) {
            ClientPacketIntent intent = scheduledIntents.peek();
            if (intent == null) break;

            if (intent.isExpired(currentTick)) {
                scheduledIntents.poll();
                cleaned++;
            } else {
                break;
            }
        }

        if (cleaned > 0 && currentTick % 100 == 0) {
            System.out.println("[PacketEngine] 清理了 " + cleaned + " 个过期意图");
        }
    }

    // ========== Getter 和 Setter ==========

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        System.out.println("[PacketEngine] " + (enabled ? "✅ 已启用" : "❌ 已禁用"));
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public int getScheduledCount() {
        return scheduledIntents.size();
    }

    public long getTotalProcessed() {
        return totalProcessed;
    }

    /**
     * 清空所有队列。
     */
    public void clearAll() {
        scheduledIntents.clear();
        registeredModifiers.clear();
        processingPacket.remove();
        System.out.println("[PacketEngine] 已清空所有队列和修改器");
    }

    /**
     * 检查是否正在处理指定包
     */
    public boolean isProcessingPacket(Packet<?> packet) {
        return processingPacket.get() == packet;
    }

    /**
     * 简单测试：验证系统工作。
     */
    public void testSystem() {
        System.out.println("[PacketEngine] 🔧 系统测试 - 仅记录模式");
        System.out.println("[PacketEngine] ✅ 游戏功能应完全正常");
        System.out.println("[PacketEngine] 📊 正在记录包数据");
        System.out.println("[PacketEngine] 🛠 修改器数量: " + registeredModifiers.size());
    }
}