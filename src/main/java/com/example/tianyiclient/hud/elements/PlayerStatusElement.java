package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示玩家状态（生命值、护甲、饥饿值等）
 */
public class PlayerStatusElement extends HudElement {

    public PlayerStatusElement() {
        super("玩家状态", 200.0f, 10.0f);

        // 添加设置
        getStyleGroup().add(new ColorSetting("文字颜色", "玩家状态 - 文字颜色", 0xFFFFFFFF));
        getStyleGroup().add(new ColorSetting("背景颜色", "玩家状态 - 背景颜色", 0x80000000));
        getStyleGroup().add(new BoolSetting("显示护甲", "玩家状态 - 显示护甲值", true));
        getStyleGroup().add(new BoolSetting("显示饱和度", "玩家状态 - 显示饥饿饱和度", true));
        getStyleGroup().add(new BoolSetting("显示效果", "玩家状态 - 显示状态效果数量", true));
        getStyleGroup().add(new BoolSetting("按住Shift显示坐标", "玩家状态 - 按住Shift显示坐标", true));
        getStyleGroup().add(new BoolSetting("阴影", "玩家状态 - 文字阴影", true));

        // 设置初始大小
        setSize(120, 60);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null || !isVisible()) return;

        updateFromSettings();

        // 获取设置值
        int textColor = getSettingValue(getStyleGroup(), "文字颜色", Integer.class, 0xFFFFFFFF);
        int bgColor = getSettingValue(getStyleGroup(), "背景颜色", Integer.class, 0x80000000);
        Boolean showArmor = getSettingValue(getStyleGroup(), "显示护甲", Boolean.class, true);
        Boolean showSaturation = getSettingValue(getStyleGroup(), "显示饱和度", Boolean.class, true);
        Boolean showEffects = getSettingValue(getStyleGroup(), "显示效果", Boolean.class, true);
        Boolean showCoordsOnShift = getSettingValue(getStyleGroup(), "按住Shift显示坐标", Boolean.class, true);
        Boolean shadow = getSettingValue(getStyleGroup(), "阴影", Boolean.class, true);

        // 获取玩家数据
        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        int armor = mc.player.getArmor();
        int food = mc.player.getHungerManager().getFoodLevel();
        float saturation = mc.player.getHungerManager().getSaturationLevel();

        // 构建显示行
        List<String> lines = new ArrayList<>();
        lines.add(String.format("❤ %.1f/%.1f", health, maxHealth));

        if (showArmor && armor > 0) {
            lines.add(String.format("🛡 %d", armor));
        }

        if (showSaturation) {
            lines.add(String.format("🍗 %d (%.1f)", food, saturation));
        } else {
            lines.add(String.format("🍗 %d", food));
        }

        if (showEffects && !mc.player.getStatusEffects().isEmpty()) {
            lines.add(String.format("✨ %d 效果", mc.player.getStatusEffects().size()));
        }

        // 按住Shift显示坐标
        if (showCoordsOnShift && mc.options.sneakKey.isPressed()) {
            lines.add(String.format("📍 %.0f %.0f %.0f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
            lines.add("🧭 " + getDirection(mc.player.getYaw()));
        }

        // 计算大小
        int maxWidth = 0;
        for (String line : lines) {
            int width = mc.textRenderer.getWidth(line);
            if (width > maxWidth) maxWidth = width;
        }

        int boxWidth = maxWidth + 12;
        int boxHeight = 10 + (lines.size() * 11);

        // 设置位置（如果太靠右，向左移动）
        int screenWidth = mc.getWindow().getScaledWidth();
        float x = getX();
        if (x + boxWidth > screenWidth) {
            x = screenWidth - boxWidth - 10;
            setPosition(x, getY());
        }

        // 渲染背景
        context.fill((int) x, (int) getY(),
                (int) (x + boxWidth), (int) (getY() + boxHeight),
                bgColor);

        // 渲染文本
        int lineY = (int) getY() + 3;
        for (String line : lines) {
            context.drawText(mc.textRenderer, Text.literal(line),
                    (int) x + 6, lineY, textColor, shadow);
            lineY += 11;
        }

        // 更新大小
        setSize(boxWidth, boxHeight);
    }

    private String getDirection(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 337.5 || yaw < 22.5) return "南";
        else if (yaw >= 22.5 && yaw < 67.5) return "西南";
        else if (yaw >= 67.5 && yaw < 112.5) return "西";
        else if (yaw >= 112.5 && yaw < 157.5) return "西北";
        else if (yaw >= 157.5 && yaw < 202.5) return "北";
        else if (yaw >= 202.5 && yaw < 247.5) return "东北";
        else if (yaw >= 247.5 && yaw < 292.5) return "东";
        else return "东南";
    }
}