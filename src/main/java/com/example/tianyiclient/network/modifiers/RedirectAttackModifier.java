package com.example.tianyiclient.network.modifiers;

import com.example.tianyiclient.network.PacketModifier;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.lang.reflect.Field;

/**
 * 重定向攻击目标修改器
 * 将攻击包的目标实体ID改为指定的实体ID
 */
public class RedirectAttackModifier implements PacketModifier {

    private final int targetEntityId;
    private final String name;

    public RedirectAttackModifier(int targetEntityId) {
        this.targetEntityId = targetEntityId;
        this.name = "重定向攻击修改器[目标ID:" + targetEntityId + "]";
    }

    @Override
    public Packet<?> modify(Packet<?> original) {
        if (!(original instanceof PlayerInteractEntityC2SPacket)) {
            System.out.println("[RedirectAttack] ⚠ 不是实体交互包，无法重定向: " + original.getClass().getSimpleName());
            return original; // 返回原包
        }

        PlayerInteractEntityC2SPacket attackPacket = (PlayerInteractEntityC2SPacket) original;

        try {
            // 使用反射修改实体ID（因为Minecraft的包字段通常是私有的）
            Field entityIdField = PlayerInteractEntityC2SPacket.class.getDeclaredField("entityId");
            entityIdField.setAccessible(true);

            int originalId = entityIdField.getInt(attackPacket);
            entityIdField.setInt(attackPacket, targetEntityId);

            System.out.println("[RedirectAttack] ✅ 重定向攻击: " + originalId + " → " + targetEntityId);

            return attackPacket;

        } catch (NoSuchFieldException e) {
            System.err.println("[RedirectAttack] ❌ 找不到entityId字段，Minecraft版本可能不兼容");
            // 尝试其他可能的字段名
            return tryAlternativeFields(attackPacket);
        } catch (Exception e) {
            System.err.println("[RedirectAttack] ❌ 修改失败: " + e.getMessage());
            e.printStackTrace();
            return original; // 出错时返回原包
        }
    }

    /**
     * 尝试其他可能的字段名（兼容不同版本）
     */
    private Packet<?> tryAlternativeFields(PlayerInteractEntityC2SPacket packet) {
        String[] possibleFields = {"entityId", "field_12891", "a", "entity"};

        for (String fieldName : possibleFields) {
            try {
                Field field = PlayerInteractEntityC2SPacket.class.getDeclaredField(fieldName);
                if (field.getType() == int.class) {
                    field.setAccessible(true);
                    int originalId = field.getInt(packet);
                    field.setInt(packet, targetEntityId);
                    System.out.println("[RedirectAttack] ✅ 使用备用字段重定向: " + fieldName);
                    return packet;
                }
            } catch (Exception e) {
                // 继续尝试下一个字段
            }
        }

        System.err.println("[RedirectAttack] ❌ 所有字段尝试失败，无法重定向");
        return packet;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ModifierType getType() {
        return ModifierType.REDIRECT_ATTACK;
    }

    public int getTargetEntityId() {
        return targetEntityId;
    }
}