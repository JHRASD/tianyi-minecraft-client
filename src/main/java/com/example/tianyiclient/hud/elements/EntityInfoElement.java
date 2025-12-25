package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.modules.render.EntityInfoModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 实体信息HUD元素
 */
public class EntityInfoElement extends HudElement {
    private final EntityInfoModule entityModule;

    public EntityInfoElement(EntityInfoModule module) {
        super("实体信息", 10, 100);
        this.entityModule = module;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!entityModule.isEnabled()) {
            return;
        }

        // 使用新增的方法而不是getSettingByName
        if (entityModule.shouldShowTargetInfo()) {
            renderTargetedEntity(context);
        }

        if (entityModule.shouldShowEntityList()) {
            renderEntityList(context);
        }
    }

    /**
     * 渲染十字准星锁定的实体信息
     */
    private void renderTargetedEntity(DrawContext context) {
        Entity target = entityModule.getTargetedEntity();
        if (target == null) return;

        int startX = (int) x;
        int startY = (int) y;
        int lineHeight = mc.textRenderer.fontHeight + 2;

        // 背景 (半透明黑色: Alpha=0x80=128)
        int bgWidth = 150;
        int bgHeight = lineHeight * 4 + 10; // 增加到4行
        context.fill(startX, startY, startX + bgWidth, startY + bgHeight, 0x80000000);

        // 实体名称 (白色，完全不透明: Alpha=0xFF=255)
        String name = entityModule.getEntityDisplayName(target);
        context.drawText(mc.textRenderer, Text.literal("目标: " + name),
                startX + 5, startY + 5, 0xFFFFFFFF, true);

        // 距离 (灰色，完全不透明)
        String distance = entityModule.getDistanceInfo(target);
        context.drawText(mc.textRenderer, Text.literal("距离: " + distance),
                startX + 5, startY + 5 + lineHeight, 0xFFAAAAAA, false);

        // 实体类型 (蓝色，完全不透明)
        String type = getEntityType(target);
        context.drawText(mc.textRenderer, Text.literal("类型: " + type),
                startX + 5, startY + 5 + lineHeight * 2, 0xFF55AAFF, false);

        // 健康信息（如果是生物）(红色，完全不透明)
        if (target instanceof LivingEntity) {
            String health = entityModule.getHealthInfo((LivingEntity) target);
            context.drawText(mc.textRenderer, Text.literal("生命: " + health),
                    startX + 5, startY + 5 + lineHeight * 3, 0xFFFF5555, false);
        }

        // 更新元素大小
        setSize(bgWidth, bgHeight);
    }

    /**
     * 获取实体类型字符串
     */
    private String getEntityType(Entity entity) {
        if (entity instanceof PlayerEntity) return "玩家";
        if (entity instanceof LivingEntity) return "生物";
        if (entity instanceof ItemEntity) return "物品";
        return "实体";
    }

    /**
     * 渲染附近实体列表
     */
    private void renderEntityList(DrawContext context) {
        List<Entity> entities = entityModule.getNearbyEntities();
        if (entities.isEmpty()) return;

        int startX = mc.getWindow().getScaledWidth() - 180;
        int startY = 50;
        int lineHeight = mc.textRenderer.fontHeight + 2;

        int maxLines = Math.min(10, entities.size());
        int bgWidth = 170;
        int bgHeight = maxLines * lineHeight + 15; // 增加标题行高度

        // 绘制背景 (半透明黑色)
        context.fill(startX, startY, startX + bgWidth, startY + bgHeight, 0x80000000);

        // 标题 (白色，完全不透明)
        String title = String.format("附近实体 (%d)", entities.size());
        context.drawText(mc.textRenderer, Text.literal(title),
                startX + 5, startY + 5, 0xFFFFFFFF, true);

        // 绘制分隔线 (半透明白色)
        context.fill(startX + 5, startY + 5 + lineHeight,
                startX + bgWidth - 5, startY + 5 + lineHeight + 1, 0x66FFFFFF);

        // 绘制实体列表
        int currentY = startY + 5 + lineHeight + 3;
        for (int i = 0; i < maxLines; i++) {
            Entity entity = entities.get(i);
            String name = entityModule.getEntityDisplayName(entity);
            String distance = entityModule.getDistanceInfo(entity);

            // 根据实体类型设置颜色 (全部使用8位ARGB格式)
            int color = getEntityColor(entity);

            String line = String.format("• %s [%s]", name, distance);
            context.drawText(mc.textRenderer, Text.literal(line),
                    startX + 5, currentY, color, false);

            currentY += lineHeight;
        }

        // 更新元素大小（如果需要在编辑模式显示）
        setSize(bgWidth, bgHeight);
    }

    /**
     * 根据实体类型获取颜色 (8位ARGB格式)
     */
    private int getEntityColor(Entity entity) {
        if (entity instanceof PlayerEntity) return 0xFF55FF55; // 玩家绿色，Alpha=0xFF
        if (entity instanceof LivingEntity) return 0xFFFF5555; // 生物红色，Alpha=0xFF
        if (entity instanceof ItemEntity) return 0xFFFFFF55;   // 物品黄色，Alpha=0xFF
        return 0xFFAAAAAA; // 默认灰色，Alpha=0xFF
    }
}