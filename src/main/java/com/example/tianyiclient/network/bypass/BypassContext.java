package com.example.tianyiclient.network.bypass;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 绕过策略上下文。
 * 包含策略决策时所需的所有瞬时游戏状态信息。
 * 这是一个不可变的数据快照，通常每游戏刻由 BypassManager 更新一次。
 */
public class BypassContext {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // ---------- 核心玩家状态 ----------
    private final ClientPlayerEntity player;
    private final Vec3d playerPos;
    private final Vec3d playerVelocity;
    private final float playerHealth;
    private final boolean isSneaking;
    private final boolean isSprinting;
    private final boolean isUsingItem;

    // ---------- 网络与时间状态 ----------
    private final long currentGameTick;
    private final float currentPartialTick;
    private final long serverPingMs; // 估算的服务器延迟（毫秒）
    private final double tickDelta; // 用于时间插值

    // ---------- 环境与风险状态 ----------
    private final int surroundingEntityCount;
    private final double riskLevel; // 综合风险评估（0.0-1.0），未来可由其他模块计算

    // 私有构造器，强制使用建造者模式
    private BypassContext(Builder builder) {
        this.player = builder.player;
        this.playerPos = builder.playerPos;
        this.playerVelocity = builder.playerVelocity;
        this.playerHealth = builder.playerHealth;
        this.isSneaking = builder.isSneaking;
        this.isSprinting = builder.isSprinting;
        this.isUsingItem = builder.isUsingItem;
        this.currentGameTick = builder.currentGameTick;
        this.currentPartialTick = builder.currentPartialTick;
        this.serverPingMs = builder.serverPingMs;
        this.tickDelta = builder.tickDelta;
        this.surroundingEntityCount = builder.surroundingEntityCount;
        this.riskLevel = builder.riskLevel;
    }

    // ========== Getter 方法 ==========
    public ClientPlayerEntity getPlayer() { return player; }
    public Vec3d getPlayerPos() { return playerPos; }
    public Vec3d getPlayerVelocity() { return playerVelocity; }
    public float getPlayerHealth() { return playerHealth; }
    public boolean isSneaking() { return isSneaking; }
    public boolean isSprinting() { return isSprinting; }
    public boolean isUsingItem() { return isUsingItem; }
    public long getCurrentGameTick() { return currentGameTick; }
    public float getCurrentPartialTick() { return currentPartialTick; }
    public long getServerPingMs() { return serverPingMs; }
    public double getTickDelta() { return tickDelta; }
    public int getSurroundingEntityCount() { return surroundingEntityCount; }
    public double getRiskLevel() { return riskLevel; }

    /**
     * 创建一个代表“当前”游戏状态的上下文快照。
     * 这是最主要的工厂方法，由 BypassManager 调用。
     * @param tickEvent 当前的游戏刻事件，提供 tick 计数
     * @return 新的 BypassContext 实例
     */
    public static BypassContext snapshotCurrent(com.example.tianyiclient.event.events.client.TickEvent tickEvent) {
        ClientPlayerEntity currentPlayer = MC.player;
        if (currentPlayer == null) {
            // 如果玩家不存在（如在菜单界面），返回一个最小的空上下文
            return empty();
        }

        return new Builder()
                .player(currentPlayer)
                .playerPos(currentPlayer.getPos())
                .playerVelocity(currentPlayer.getVelocity())
                .playerHealth(currentPlayer.getHealth())
                .isSneaking(currentPlayer.isSneaking())
                .isSprinting(currentPlayer.isSprinting())
                .isUsingItem(currentPlayer.isUsingItem())
                .currentGameTick(tickEvent.getTickCount())
                .currentPartialTick(tickEvent.getPartialTicks())
                .serverPingMs(estimateServerPing()) // 需要实现估算方法
                .tickDelta(1.0f / 20.0f) // 默认 20 TPS，后续可优化
                .surroundingEntityCount(estimateNearbyEntities(currentPlayer))
                .riskLevel(0.3) // 默认基础风险，后续可由其他模块动态计算
                .build();
    }

    /**
     * 创建一个空的/默认的上下文（用于无玩家时）。
     */
    public static BypassContext empty() {
        return new Builder().build();
    }

    // ---------- 内部估算方法（占位符） ----------
    private static long estimateServerPing() {
        // 这里可以接入你的网络监测模块，或简单返回一个值
        // 例如：return MC.getNetworkHandler() != null ? MC.getNetworkHandler().getPlayerListEntry(MC.player.getUuid()).getLatency() : 50L;
        return 50L; // 默认 50ms
    }

    private static int estimateNearbyEntities(ClientPlayerEntity player) {
        // 简单估算：返回10格内的实体数量（不包括玩家自己）
        // 这是一个性能占位符，实际实现需优化
        if (player.clientWorld == null) return 0;
        return player.clientWorld.getOtherEntities(player, player.getBoundingBox().expand(10.0)).size();
    }

    // ========== 建造者类 ==========
    public static class Builder {
        // 提供所有字段的默认值
        private ClientPlayerEntity player = null;
        private Vec3d playerPos = Vec3d.ZERO;
        private Vec3d playerVelocity = Vec3d.ZERO;
        private float playerHealth = 20.0f;
        private boolean isSneaking = false;
        private boolean isSprinting = false;
        private boolean isUsingItem = false;
        private long currentGameTick = 0;
        private float currentPartialTick = 0.0f;
        private long serverPingMs = 50L;
        private double tickDelta = 0.05; // 1/20
        private int surroundingEntityCount = 0;
        private double riskLevel = 0.0;

        public Builder player(ClientPlayerEntity p) { this.player = p; return this; }
        public Builder playerPos(Vec3d pos) { this.playerPos = pos; return this; }
        public Builder playerVelocity(Vec3d vel) { this.playerVelocity = vel; return this; }
        public Builder playerHealth(float health) { this.playerHealth = health; return this; }
        public Builder isSneaking(boolean sneaking) { this.isSneaking = sneaking; return this; }
        public Builder isSprinting(boolean sprinting) { this.isSprinting = sprinting; return this; }
        public Builder isUsingItem(boolean using) { this.isUsingItem = using; return this; }
        public Builder currentGameTick(long tick) { this.currentGameTick = tick; return this; }
        public Builder currentPartialTick(float partial) { this.currentPartialTick = partial; return this; }
        public Builder serverPingMs(long ping) { this.serverPingMs = ping; return this; }
        public Builder tickDelta(double delta) { this.tickDelta = delta; return this; }
        public Builder surroundingEntityCount(int count) { this.surroundingEntityCount = count; return this; }
        public Builder riskLevel(double risk) { this.riskLevel = Math.max(0.0, Math.min(1.0, risk)); return this; }

        public BypassContext build() {
            return new BypassContext(this);
        }
    }
}