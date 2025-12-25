package com.example.tianyiclient.event.events.world;

import com.example.tianyiclient.event.Event;
import net.minecraft.entity.Entity;

/**
 * 实体移除事件
 * 当实体从世界中移除时触发
 */
public class EntityRemovedEvent extends Event {
    private final Entity entity;
    private final RemoveReason reason;

    /**
     * 实体移除原因
     */
    public enum RemoveReason {
        /**
         * 实体死亡
         */
        DEATH,

        /**
         * 实体被卸载（区块卸载）
         */
        UNLOAD,

        /**
         * 实体被手动移除
         */
        MANUAL,

        /**
         * 实体消失（如物品被拾取）
         */
        DESPAWN,

        /**
         * 其他原因
         */
        OTHER
    }

    public EntityRemovedEvent(Entity entity, RemoveReason reason) {
        this.entity = entity;
        this.reason = reason;
    }

    public Entity getEntity() {
        return entity;
    }

    public RemoveReason getReason() {
        return reason;
    }

    /**
     * 获取实体类型名称
     */
    public String getEntityTypeName() {
        return entity.getType().getName().getString();
    }

    /**
     * 获取实体UUID
     */
    public String getEntityUuid() {
        return entity.getUuid().toString();
    }

    /**
     * 检查是否为死亡导致的移除
     */
    public boolean isDeath() {
        return reason == RemoveReason.DEATH;
    }

    /**
     * 检查是否为卸载导致的移除
     */
    public boolean isUnload() {
        return reason == RemoveReason.UNLOAD;
    }

    @Override
    public String toString() {
        return String.format("EntityRemovedEvent{type=%s, reason=%s}",
                getEntityTypeName(), reason);
    }
}