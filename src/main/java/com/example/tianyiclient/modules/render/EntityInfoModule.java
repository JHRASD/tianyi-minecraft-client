package com.example.tianyiclient.modules.render;

import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 实体信息显示模块
 */
public class EntityInfoModule extends Module {
    // ========== 模块设置项 ==========
    private final BoolSetting showTargetInfo;
    private final BoolSetting showEntityList;
    private final DoubleSetting maxDistance;
    private final BoolSetting showPlayers;
    private final BoolSetting showMobs;
    private final BoolSetting showItems;
    private final DoubleSetting itemValueThreshold;

    public EntityInfoModule() {
        super("实体信息", "显示锁定的实体信息和附近实体列表", Category.渲染);

        // 根据你的DoubleSetting构造函数调整参数
        // 选项1：如果有描述参数
        this.showTargetInfo = new BoolSetting("显示目标信息", "显示十字准星指向的实体信息", true);
        this.showEntityList = new BoolSetting("显示实体列表", "在侧边显示附近的实体列表", false);
        this.maxDistance = new DoubleSetting("检测距离", "检测实体的最大距离（格）", 50.0, 10.0, 200.0);
        this.showPlayers = new BoolSetting("显示玩家", "在列表中显示其他玩家", true);
        this.showMobs = new BoolSetting("显示生物", "在列表中显示敌对/中立生物", true);
        this.showItems = new BoolSetting("显示物品", "显示有价值的掉落物", false);
        this.itemValueThreshold = new DoubleSetting("物品价值阈值", "只显示价值超过此值的物品", 100.0, 0.0, 10000.0);

        // 选项2：如果没有描述参数
        /*
        this.showTargetInfo = new BoolSetting("显示目标信息", true);
        this.showEntityList = new BoolSetting("显示实体列表", false);
        this.maxDistance = new DoubleSetting("检测距离", 50.0, 10.0, 200.0);
        this.showPlayers = new BoolSetting("显示玩家", true);
        this.showMobs = new BoolSetting("显示生物", true);
        this.showItems = new BoolSetting("显示物品", false);
        this.itemValueThreshold = new DoubleSetting("物品价值阈值", 100.0, 0.0, 10000.0);
        */

        // 添加所有设置项
        addSetting(showTargetInfo);
        addSetting(showEntityList);
        addSetting(maxDistance);
        addSetting(showPlayers);
        addSetting(showMobs);
        addSetting(showItems);
        addSetting(itemValueThreshold);
    }

    @Override
    protected void onEnable() {
        System.out.println("[实体信息] 模块已启用");
    }

    @Override
    protected void onDisable() {
        System.out.println("[实体信息] 模块已禁用");
    }

    /**
     * 获取十字准星锁定的实体
     */
    public Entity getTargetedEntity() {
        if (mc == null || mc.player == null || mc.crosshairTarget == null) {
            return null;
        }

        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
        return entityHit.getEntity();
    }

    /**
     * 获取实体显示名称
     */
    public String getEntityDisplayName(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return ((PlayerEntity) entity).getGameProfile().getName();
        } else if (entity.hasCustomName()) {
            return entity.getCustomName().getString();
        } else {
            return entity.getType().getName().getString();
        }
    }

    /**
     * 获取实体健康信息
     */
    public String getHealthInfo(LivingEntity entity) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        return String.format("♥ %.1f/%.1f", health, maxHealth);
    }

    /**
     * 获取实体距离
     */
    public String getDistanceInfo(Entity entity) {
        if (mc.player == null) return "0.0m";
        double distance = mc.player.distanceTo(entity);
        return String.format("%.1fm", distance);
    }

    /**
     * 检查物品实体是否值得显示
     */
    public boolean isValuableItem(ItemEntity itemEntity) {
        if (!showItems.getValue()) return false;

        int count = itemEntity.getStack().getCount();
        double threshold = itemValueThreshold.getValue();
        return count > threshold / 100;
    }

    /**
     * 获取附近的重要实体列表
     */
    public List<Entity> getNearbyEntities() {
        List<Entity> entities = new ArrayList<>();

        if (mc.world == null || mc.player == null) return entities;

        double maxDist = maxDistance.getValue();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;

            double distance = mc.player.distanceTo(entity);
            if (distance > maxDist) continue;

            if (entity instanceof PlayerEntity && showPlayers.getValue()) {
                entities.add(entity);
            } else if (entity instanceof LivingEntity && showMobs.getValue() && !(entity instanceof PlayerEntity)) {
                entities.add(entity);
            } else if (entity instanceof ItemEntity && isValuableItem((ItemEntity) entity)) {
                entities.add(entity);
            }
        }

        entities.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        return entities;
    }

    // 添加这些方法来替代直接的getValue()调用
    public boolean shouldShowTargetInfo() {
        return isEnabled() && showTargetInfo.getValue();
    }

    public boolean shouldShowEntityList() {
        return isEnabled() && showEntityList.getValue();
    }
}