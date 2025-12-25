package com.example.tianyiclient.event.events.player;

import com.example.tianyiclient.event.Event;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.text.Text;

/**
 * 玩家死亡事件
 * 适配 Fabric 1.21.8 新版伤害系统 API
 */
public class DeathEvent extends Event {
    private final DamageSource damageSource;
    private final Text deathMessage;
    private final float damageAmount;
    private final boolean isClientPlayer;
    private final DamageType damageType;

    public DeathEvent(DamageSource damageSource, Text deathMessage, float damageAmount, boolean isClientPlayer) {
        this.damageSource = damageSource;
        this.deathMessage = deathMessage;
        this.damageAmount = damageAmount;
        this.isClientPlayer = isClientPlayer;
        this.damageType = damageSource.getType(); // 新版 API 返回 DamageType 对象
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public Text getDeathMessage() {
        return deathMessage;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public boolean isClientPlayer() {
        return isClientPlayer;
    }

    /** 获取死亡消息文本 */
    public String getDeathMessageText() {
        return deathMessage.getString();
    }

    /** 获取伤害源名称 (新版 API: getName() 返回 String) */
    public String getDamageSourceName() {
        return damageSource.getName();
    }

    /** 检查是否为摔落伤害 */
    public boolean isFallDamage() {
        return damageSource.isIn(DamageTypeTags.IS_FALL);
    }

    /** 检查是否为实体攻击 */
    public boolean isEntityAttack() {
        return damageSource.getSource() != null;
    }

    /** 检查是否为火焰伤害 */
    public boolean isFireDamage() {
        return damageSource.isIn(DamageTypeTags.IS_FIRE);
    }

    /** 检查是否为爆炸伤害 */
    public boolean isExplosion() {
        return damageSource.isIn(DamageTypeTags.IS_EXPLOSION);
    }

    /** 检查是否为魔法伤害 (新版没有 IS_MAGIC 标签，改用字符串匹配) */
    public boolean isMagic() {
        String typeName = damageType.toString().toLowerCase();
        return typeName.contains("magic") || typeName.contains("witch");
    }

    /** 检查是否为环境伤害 */
    public boolean isEnvironmental() {
        String typeName = damageType.toString().toLowerCase();
        return typeName.contains("environment") ||
                typeName.contains("generic") ||
                typeName.contains("out_of_world");
    }

    /** 检查是否为溺水伤害 */
    public boolean isDrowning() {
        return damageSource.isIn(DamageTypeTags.IS_DROWNING);
    }

    /** 检查是否为饥饿伤害 (新版没有 IS_STARVING 标签，改用字符串匹配) */
    public boolean isStarving() {
        String typeName = damageType.toString().toLowerCase();
        return typeName.contains("starve") || typeName.contains("hunger");
    }

    /** 获取攻击者实体（如果有） */
    public net.minecraft.entity.Entity getAttacker() {
        return damageSource.getSource();
    }

    /** 获取攻击者名称（如果有） */
    public String getAttackerName() {
        net.minecraft.entity.Entity attacker = getAttacker();
        return attacker != null ? attacker.getDisplayName().getString() : null;
    }

    /** 获取直接攻击者（如箭矢、火球等抛射物） */
    public net.minecraft.entity.Entity getDirectAttacker() {
        return damageSource.getAttacker();
    }

    /** 获取伤害类型对象 */
    public DamageType getDamageType() {
        return damageType;
    }

    /** 获取伤害类型名称 */
    public String getDamageTypeName() {
        return damageType.toString();
    }

    @Override
    public String toString() {
        return String.format("DeathEvent{type=%s, source=%s, amount=%.1f, isClient=%s}",
                getDamageTypeName(),
                getDamageSourceName(),
                damageAmount,
                isClientPlayer);
    }
}
