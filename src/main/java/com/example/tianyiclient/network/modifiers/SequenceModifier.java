package com.example.tianyiclient.network.modifiers;

import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.PacketModifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 序列修改器
 * 在原包前后添加额外的包序列
 */
public class SequenceModifier implements PacketModifier {

    private final List<Packet<?>> prePackets = new CopyOnWriteArrayList<>();
    private final List<Packet<?>> postPackets = new CopyOnWriteArrayList<>();
    private final String name;
    private final MinecraftClient mc;
    private final PacketEngine packetEngine;
    private final ExecutorService sequenceExecutor;

    // 序列发送间隔（毫秒）
    private long sequenceInterval = 50;

    public SequenceModifier() {
        this.name = "序列修改器";
        this.mc = MinecraftClient.getInstance();
        this.packetEngine = PacketEngine.getInstance();

        // 创建序列执行器
        this.sequenceExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Packet-Sequence-Worker");
            thread.setDaemon(true);
            return thread;
        });

        // 默认添加一些示例包
        initializeDefaultPackets();
    }

    /**
     * 初始化默认包序列
     */
    private void initializeDefaultPackets() {
        try {
            // 添加挥臂动画包
            // 注意：需要根据实际的Minecraft版本调整
            System.out.println("[SequenceModifier] 初始化默认包序列");

            // 这里可以根据需要添加更多默认包
            // addPrePacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            // addPostPacket(new HandSwingC2SPacket(Hand.OFF_HAND));

        } catch (Exception e) {
            System.err.println("[SequenceModifier] ❌ 初始化默认包失败: " + e.getMessage());
        }
    }

    /**
     * 在原包前添加包
     */
    public SequenceModifier addPrePacket(Packet<?> packet) {
        if (packet != null) {
            prePackets.add(packet);
            System.out.println("[SequenceModifier] ➕ 添加前置包: " + packet.getClass().getSimpleName());
        }
        return this;
    }

    /**
     * 在原包后添加包
     */
    public SequenceModifier addPostPacket(Packet<?> packet) {
        if (packet != null) {
            postPackets.add(packet);
            System.out.println("[SequenceModifier] ➕ 添加后置包: " + packet.getClass().getSimpleName());
        }
        return this;
    }

    /**
     * 添加挥臂动画包
     */
    public SequenceModifier addSwingAnimation(Hand hand) {
        try {
            HandSwingC2SPacket swingPacket = new HandSwingC2SPacket(hand);
            addPostPacket(swingPacket);
            System.out.println("[SequenceModifier] 👋 添加挥臂动画: " + hand.name());
        } catch (Exception e) {
            System.err.println("[SequenceModifier] ❌ 创建挥臂包失败: " + e.getMessage());
        }
        return this;
    }

    /**
     * 设置序列发送间隔
     */
    public SequenceModifier setSequenceInterval(long millis) {
        this.sequenceInterval = Math.max(10, millis); // 最小10ms
        return this;
    }

    @Override
    public Packet<?> modify(Packet<?> original) {
        if (original == null) {
            return null;
        }

        final String originalName = original.getClass().getSimpleName();
        System.out.println("[SequenceModifier] 🔄 开始处理包序列: " + originalName);
        System.out.println("[SequenceModifier] 📊 前置包: " + prePackets.size() + ", 后置包: " + postPackets.size());

        // 异步执行包序列发送
        sequenceExecutor.submit(() -> {
            try {
                // 1. 发送前置包序列
                sendPacketSequence(prePackets, "前置");

                // 2. 短暂间隔（如果设置了间隔且没有前置包，不需要等待）
                if (sequenceInterval > 0 && !prePackets.isEmpty()) {
                    safeSleep(sequenceInterval);
                }

                // 3. 发送原包
                System.out.println("[SequenceModifier] 📦 发送原包: " + originalName);
                packetEngine.sendPacketSafely(original);

                // 4. 短暂间隔
                if (sequenceInterval > 0) {
                    safeSleep(sequenceInterval);
                }

                // 5. 发送后置包序列
                sendPacketSequence(postPackets, "后置");

                System.out.println("[SequenceModifier] ✅ 包序列处理完成");

                // 在聊天中显示提示（调试用）
                if (mc.player != null) {
                    String displayName = originalName;
                    if (displayName.length() > 15) {
                        displayName = displayName.substring(0, 15) + "...";
                    }
                    mc.player.sendMessage(
                            Text.literal("§8[包序列] §7" + displayName +
                                    " §8(+" + (prePackets.size() + postPackets.size()) + "个额外包)"),
                            false
                    );
                }

            } catch (Exception e) {
                System.err.println("[SequenceModifier] ❌ 序列发送失败: " + e.getMessage());

                // 出错时降级：只发送原包
                packetEngine.sendPacketSafely(original);
            }
        });

        // 返回null告诉PacketEngine不要立即发送原包
        // （原包会在序列中异步发送）
        return null;
    }

    /**
     * 安全的睡眠方法，正确处理中断
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[SequenceModifier] ⏰ 序列间隔被中断，继续执行");
            // 不抛出异常，继续执行
        }
    }

    /**
     * 发送包序列（改进版本）
     */
    private void sendPacketSequence(List<Packet<?>> packets, String sequenceType) {
        if (packets.isEmpty()) {
            return;
        }

        System.out.println("[SequenceModifier] 📤 发送" + sequenceType + "包序列 (" + packets.size() + "个)");

        for (int i = 0; i < packets.size(); i++) {
            Packet<?> packet = packets.get(i);
            if (packet == null) continue;

            try {
                // 发送包
                packetEngine.sendPacketSafely(packet);
                System.out.println("[SequenceModifier]   " + (i+1) + ". " + packet.getClass().getSimpleName());

                // 包之间添加微小间隔（模拟人类操作）
                if (i < packets.size() - 1 && sequenceInterval > 0) {
                    safeSleep(sequenceInterval / 2); // 使用安全的睡眠方法
                }

            } catch (Exception e) {
                System.err.println("[SequenceModifier] ⚠ 发送" + sequenceType + "包失败 (" + (i+1) + "/" + packets.size() + "): " + e.getMessage());
                // 继续发送下一个包
            }
        }
    }

    /**
     * 清空所有包序列
     */
    public void clearAllPackets() {
        prePackets.clear();
        postPackets.clear();
        System.out.println("[SequenceModifier] 已清空所有包序列");
    }

    /**
     * 获取前置包数量
     */
    public int getPrePacketCount() {
        return prePackets.size();
    }

    /**
     * 获取后置包数量
     */
    public int getPostPacketCount() {
        return postPackets.size();
    }

    /**
     * 关闭执行器（清理资源）
     */
    public void shutdown() {
        sequenceExecutor.shutdown();
        try {
            if (!sequenceExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                sequenceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            sequenceExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        clearAllPackets();
        System.out.println("[SequenceModifier] 执行器已关闭");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ModifierType getType() {
        return ModifierType.SEQUENCE;
    }

    public List<Packet<?>> getPrePackets() {
        return new ArrayList<>(prePackets);
    }

    public List<Packet<?>> getPostPackets() {
        return new ArrayList<>(postPackets);
    }

    public long getSequenceInterval() {
        return sequenceInterval;
    }
}