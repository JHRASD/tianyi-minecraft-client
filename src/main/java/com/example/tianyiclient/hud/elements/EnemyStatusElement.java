package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.*;

/**
 * 现代极简敌人信息显示器
 * 设计理念: 文字信息流 + 智能配色 + 去图标化
 */
public class EnemyStatusElement extends HudElement {

    private final List<LivingEntity> nearbyEnemies = new ArrayList<>();
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL = 800;
    private static final int DETECTION_RANGE = 36;

    // 现代配色系统
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;    // 主文字颜色
    private static final int TEXT_SECONDARY = 0xB3FFFFFF;   // 次要文字颜色
    private static final int TEXT_SAFE = 0xFF4CAF50;       // 安全状态颜色
    private static final int TEXT_WARNING = 0xFFFF9800;    // 警告状态颜色
    private static final int TEXT_DANGER = 0xFFF44336;     // 危险状态颜色
    private static final int TEXT_PLAYER = 0xFF2196F3;     // 玩家特殊颜色

    // 背景色
    private static final int BG_DEFAULT = 0xCC1A1A2E;      // 默认背景
    private static final int BG_HIGHLIGHT = 0xCC25253A;    // 高亮背景

    public EnemyStatusElement() {
        super("目标追踪", 400.0f, 10.0f);
        setSize(160, 40);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.world == null || mc.player == null || !isVisible()) return;

        scanNearbyEnemies();
        renderModernTextFlow(context);
    }

    /**
     * 扫描附近目标（包括敌对生物和玩家）
     */
    private void scanNearbyEnemies() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime < SCAN_INTERVAL) return;

        nearbyEnemies.clear();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        Box searchBox = player.getBoundingBox().expand(DETECTION_RANGE);
        List<Entity> entities = mc.world.getOtherEntities(player, searchBox);

        for (Entity entity : entities) {
            // 包括敌对生物
            if (entity instanceof HostileEntity) {
                nearbyEnemies.add((LivingEntity) entity);
            }
            // 也可以包括其他玩家（如果需要追踪玩家）
            else if (entity instanceof PlayerEntity && entity != player) {
                nearbyEnemies.add((LivingEntity) entity);
            }
        }

        // 按威胁等级排序
        nearbyEnemies.sort((e1, e2) -> {
            double threat1 = calculateThreatScore(e1);
            double threat2 = calculateThreatScore(e2);
            return Double.compare(threat2, threat1);
        });

        lastScanTime = currentTime;
    }

    /**
     * 渲染现代文字信息流
     */
    private void renderModernTextFlow(DrawContext context) {
        int x = (int) getX();
        int y = (int) getY();

        if (nearbyEnemies.isEmpty()) {
            renderSafeState(context, x, y);
            setSize(120, 28);
        } else {
            renderTargetList(context, x, y);
        }
    }

    /**
     * 安全状态显示
     */
    private void renderSafeState(DrawContext context, int x, int y) {
        int width = 120;
        int height = 28;

        // 极简半透明背景
        context.fill(x, y, x + width, y + height, BG_DEFAULT);

        // 顶部状态线
        context.fill(x, y, x + width, y + 1, TEXT_SAFE);

        // 状态文字
        context.drawText(mc.textRenderer, "● 区域安全", x + 10, y + 8, TEXT_SAFE, true);
        context.drawText(mc.textRenderer, "无追踪目标", x + 10, y + 18, TEXT_SECONDARY, true);

        setSize(width, height);
    }

    /**
     * 目标列表显示
     */
    private void renderTargetList(DrawContext context, int x, int y) {
        int maxDisplay = Math.min(nearbyEnemies.size(), 3); // 最多显示3个
        int lineHeight = 26; // 每行高度
        int panelHeight = 10 + (maxDisplay * lineHeight); // 10像素顶部边距
        int panelWidth = 180;

        // 现代卡片背景
        drawModernCard(context, x, y, panelWidth, panelHeight);

        // 标题
        context.drawText(mc.textRenderer, "追踪目标", x + 10, y + 6, TEXT_PRIMARY, true);
        String countText = nearbyEnemies.size() + "个";
        int countWidth = mc.textRenderer.getWidth(countText);
        context.drawText(mc.textRenderer, countText, x + panelWidth - countWidth - 10, y + 6, TEXT_SECONDARY, true);

        // 分割线
        context.fill(x + 10, y + 20, x + panelWidth - 10, y + 21, 0x33FFFFFF);

        // 显示每个目标
        int startY = y + 26;
        for (int i = 0; i < maxDisplay; i++) {
            renderTargetLine(context, x + 10, startY + (i * lineHeight), nearbyEnemies.get(i));
        }

        setSize(panelWidth, panelHeight);
    }

    /**
     * 绘制现代卡片背景
     */
    private void drawModernCard(DrawContext context, int x, int y, int width, int height) {
        // 主背景
        context.fill(x, y, x + width, y + height, BG_DEFAULT);

        // 顶部高光
        context.fill(x, y, x + width, y + 2, 0x33FFFFFF);

        // 底部阴影
        context.fill(x, y + height - 2, x + width, y + height, 0x33000000);
    }

    /**
     * 渲染单个目标行
     */
    private void renderTargetLine(DrawContext context, int x, int y, LivingEntity target) {
        double distance = Math.sqrt(mc.player.squaredDistanceTo(target));
        float healthPercent = target.getHealth() / target.getMaxHealth();

        // 判断目标类型
        boolean isPlayer = target instanceof PlayerEntity;

        // 第一行：名称 + 距离
        String name = getTargetName(target);
        String distanceText = String.format("%.1fm", distance);

        // 根据距离决定颜色
        int nameColor = getDistanceColor(distance, isPlayer);
        int distanceColor = TEXT_SECONDARY;

        // 绘制名称
        context.drawText(mc.textRenderer, name, x, y, nameColor, true);

        // 绘制距离（右对齐）
        int distanceX = x + 130 - mc.textRenderer.getWidth(distanceText);
        context.drawText(mc.textRenderer, distanceText, distanceX, y, distanceColor, true);

        // 第二行：血量条 + 百分比
        renderCompactHealthBar(context, x, y + 12, healthPercent, isPlayer);
    }

    /**
     * 获取目标名称
     */
    private String getTargetName(LivingEntity target) {
        if (target instanceof PlayerEntity) {
            // 对于玩家，显示玩家名
            return target.getName().getString();
        } else {
            // 对于生物，显示生物名称
            String name = target.getType().getName().getString();

            // 美化生物名称（移除命名空间和_符号）
            if (name.contains(".")) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }
            name = name.replace("_", " ");

            // 首字母大写
            if (!name.isEmpty()) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }

            return name;
        }
    }

    /**
     * 渲染紧凑血量条
     */
    private void renderCompactHealthBar(DrawContext context, int x, int y, float healthPercent, boolean isPlayer) {
        int barWidth = 100;
        int barHeight = 3;

        // 血量百分比文字
        String percentText = String.format("%.0f%%", healthPercent * 100);
        int percentColor = getHealthColor(healthPercent, isPlayer);

        // 背景
        context.fill(x, y, x + barWidth, y + barHeight, 0x44FFFFFF);

        // 血量填充
        int fillWidth = (int) (barWidth * healthPercent);
        if (fillWidth > 0) {
            context.fill(x, y, x + fillWidth, y + barHeight, percentColor);
        }

        // 百分比文字（在进度条右侧）
        context.drawText(mc.textRenderer, percentText, x + barWidth + 6, y - 3, percentColor, true);
    }

    /**
     * 根据距离获取颜色
     */
    private int getDistanceColor(double distance, boolean isPlayer) {
        if (isPlayer) {
            return TEXT_PLAYER; // 玩家始终用蓝色
        }

        if (distance < 8) return TEXT_DANGER;    // 8格内：红色（危险）
        if (distance < 16) return TEXT_WARNING;  // 8-16格：橙色（警告）
        return TEXT_SAFE;                        // 16格外：绿色（安全）
    }

    /**
     * 根据血量获取颜色
     */
    private int getHealthColor(float percent, boolean isPlayer) {
        if (isPlayer) {
            // 玩家血量颜色稍微不同
            if (percent > 0.7) return 0xFF4FC3F7; // 亮蓝色
            if (percent > 0.3) return 0xFFFFB74D; // 橙色
            return 0xFFE57373; // 浅红色
        }

        if (percent > 0.7) return TEXT_SAFE;     // 绿色
        if (percent > 0.3) return TEXT_WARNING;  // 橙色
        return TEXT_DANGER;                      // 红色
    }

    /**
     * 计算威胁评分
     */
    private double calculateThreatScore(LivingEntity target) {
        double score = 0;
        double distance = Math.sqrt(mc.player.squaredDistanceTo(target));

        // 距离权重（越近评分越高）
        score += (40 - Math.min(distance, 40));

        // 血量权重
        float healthPercent = target.getHealth() / target.getMaxHealth();
        score += healthPercent * 30;

        // 玩家额外权重（如果追踪玩家）
        if (target instanceof PlayerEntity) {
            score += 20;
        }

        // 特定生物类型权重
        score += getEntityTypeWeight(target);

        return score;
    }

    /**
     * 获取生物类型权重
     */
    private int getEntityTypeWeight(LivingEntity entity) {
        String typeName = entity.getType().toString().toLowerCase();

        if (typeName.contains("creeper")) return 15; // 苦力怕
        if (typeName.contains("witch")) return 12;   // 女巫
        if (typeName.contains("skeleton")) return 8; // 骷髅
        if (typeName.contains("spider")) return 6;   // 蜘蛛

        return 5; // 基础权重
    }
}