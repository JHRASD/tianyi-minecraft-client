package com.example.tianyiclient.event.events.world;

import com.example.tianyiclient.event.Event;
import net.minecraft.entity.Entity;

/**
 * 实体添加事件
 * 当新实体被添加到世界时触发
 */
public class EntityAddedEvent extends Event {
    private final Entity entity;

    public EntityAddedEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
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
     * 检查是否为玩家实体
     */
    public boolean isPlayer() {
        return entity.getType() == net.minecraft.entity.EntityType.PLAYER;
    }

    /**
     * 检查是否为生物实体
     */
    public boolean isLivingEntity() {
        return entity instanceof net.minecraft.entity.LivingEntity;
    }

    /**
     * 检查是否为物品实体
     */
    public boolean isItemEntity() {
        return entity instanceof net.minecraft.entity.ItemEntity;
    }

    /**
     * 检查是否为抛射物
     */
    public boolean isProjectile() {
        return entity instanceof net.minecraft.entity.projectile.ProjectileEntity;
    }

    @Override
    public String toString() {
        return String.format("EntityAddedEvent{type=%s, uuid=%s}",
                getEntityTypeName(), getEntityUuid());
    }
}