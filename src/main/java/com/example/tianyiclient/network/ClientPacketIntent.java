package com.example.tianyiclient.network;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * 客户端数据包意图
 * 表示一个模块希望在未来某个时刻执行的数据包操作。
 * 这是一个不可变的数据容器。
 */
public class ClientPacketIntent {

    /** 意图类型枚举 */
    public enum IntentType {
        /** 攻击一个实体 */
        ATTACK_ENTITY,
        /** 与一个实体交互（右键） */
        INTERACT_ENTITY,
        /** 在某个位置放置方块 */
        USE_BLOCK,
        /** 丢出一个物品 */
        THROW_ITEM,
        /** 开始或停止潜行（用于组合技） */
        CHANGE_SNEAK_STATE,
        /** 丢弃物品（按Q） */
        DROP_ITEM,
        /** 释放使用物品（右键释放） */
        RELEASE_USE_ITEM,
        /** 切换副手物品 */
        SWAP_ITEMS
    }

    private final IntentType type;
    private final Entity targetEntity; // 对于非实体目标，可为null
    private final Vec3d targetPosition; // 对于位置目标，可为null
    private final Object extraData; // 扩展数据，如物品栈
    private final long scheduleTick; // 计划在哪一个游戏刻执行
    private final int timeoutTicks; // 超时时间（刻），超时后意图会被丢弃
    private final String sourceModule; // 发起此意图的模块名，用于调试

    // 主构造方法，使用建造者模式创建
    private ClientPacketIntent(Builder builder) {
        this.type = builder.type;
        this.targetEntity = builder.targetEntity;
        this.targetPosition = builder.targetPosition;
        this.extraData = builder.extraData;
        this.scheduleTick = builder.scheduleTick;
        this.timeoutTicks = builder.timeoutTicks;
        this.sourceModule = builder.sourceModule;
    }

    // ========== Getter 方法 ==========
    public IntentType getType() { return type; }
    public Entity getTargetEntity() { return targetEntity; }
    public Vec3d getTargetPosition() { return targetPosition; }
    public Object getExtraData() { return extraData; }
    public long getScheduleTick() { return scheduleTick; }
    public int getTimeoutTicks() { return timeoutTicks; }
    public String getSourceModule() { return sourceModule; }

    /**
     * 判断此意图是否已过期
     * @param currentTick 当前游戏刻
     * @return 是否过期
     */
    public boolean isExpired(long currentTick) {
        return currentTick > scheduleTick + timeoutTicks;
    }

    /**
     * 判断此意图是否已到执行时间
     * @param currentTick 当前游戏刻
     * @return 是否可以执行
     */
    public boolean isReady(long currentTick) {
        return currentTick >= scheduleTick && !isExpired(currentTick);
    }

    // ========== 建造者类 ==========
    public static class Builder {
        private final IntentType type;
        private Entity targetEntity = null;
        private Vec3d targetPosition = null;
        private Object extraData = null;
        private long scheduleTick = 0;
        private int timeoutTicks = 40; // 默认2秒超时 (20tick/s * 2)
        private String sourceModule = "unknown";

        public Builder(IntentType type) {
            this.type = type;
        }

        public Builder targetEntity(Entity entity) {
            this.targetEntity = entity;
            return this;
        }

        public Builder targetPosition(Vec3d pos) {
            this.targetPosition = pos;
            return this;
        }

        public Builder extraData(Object data) {
            this.extraData = data;
            return this;
        }

        public Builder scheduleTick(long tick) {
            this.scheduleTick = tick;
            return this;
        }

        public Builder timeoutTicks(int ticks) {
            this.timeoutTicks = ticks;
            return this;
        }

        public Builder sourceModule(String module) {
            this.sourceModule = module;
            return this;
        }

        public ClientPacketIntent build() {
            return new ClientPacketIntent(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Intent[%s from %s @ tick %d]",
                type, sourceModule, scheduleTick);
    }
}