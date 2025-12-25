package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * 显示玩家坐标和方向
 */
public class CoordinatesElement extends HudElement {

    public CoordinatesElement() {
        super("坐标信息", 10.0f, 50.0f);

        // 添加设置
        getStyleGroup().add(new ColorSetting("文字颜色", "坐标 - 文字颜色", 0xFFFFFFFF));
        getStyleGroup().add(new BoolSetting("显示方向", "坐标 - 显示方向", true));
        getStyleGroup().add(new BoolSetting("显示地狱坐标", "坐标 - 显示地狱坐标", true));
        getStyleGroup().add(new BoolSetting("显示高度", "坐标 - 显示高度(Y)", true));
        getStyleGroup().add(new BoolSetting("阴影", "坐标 - 文字阴影", true));
        getStyleGroup().add(new IntegerSetting("字体大小", "坐标 - 字体大小", 9, 6, 24));

        // 设置初始大小
        setSize(150, 30);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null || !isVisible()) return;

        updateFromSettings();

        // 获取设置值
        int color = getSettingValue(getStyleGroup(), "文字颜色", Integer.class, 0xFFFFFFFF);
        Boolean showDirection = getSettingValue(getStyleGroup(), "显示方向", Boolean.class, true);
        Boolean showNether = getSettingValue(getStyleGroup(), "显示地狱坐标", Boolean.class, true);
        Boolean showHeight = getSettingValue(getStyleGroup(), "显示高度", Boolean.class, true);
        Boolean shadow = getSettingValue(getStyleGroup(), "阴影", Boolean.class, true);
        int fontSize = getSettingValue(getStyleGroup(), "字体大小", Integer.class, 9);

        // 获取玩家数据
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        float yaw = mc.player.getYaw();

        // 构建显示文本
        StringBuilder text = new StringBuilder();
        text.append("坐标: ");

        if (showHeight) {
            text.append(String.format("%.1f, %.1f, %.1f", x, y, z));
        } else {
            text.append(String.format("%.1f, %.1f", x, z));
        }

        if (showDirection) {
            text.append(" (").append(getDirection(yaw)).append(")");
        }

        // 地狱坐标
        if (showNether && mc.world != null && mc.world.getRegistryKey().getValue().getPath().equals("overworld")) {
            text.append("\n地狱: ").append(String.format("%.1f, %.1f", x / 8, z / 8));
        }

        // 渲染背景
        int bgAlpha = (int)(0x80 * getAlpha());
        int bgColor = (bgAlpha << 24) | 0x000000;
        context.fill((int) getX(), (int) getY(),
                (int) (getX() + getWidth()), (int) (getY() + getHeight()),
                bgColor);

        // 渲染文本
        int textAlpha = (int)(0xFF * getAlpha());
        int textColor = (textAlpha << 24) | (color & 0x00FFFFFF);

        // 分割行渲染
        String[] lines = text.toString().split("\n");
        int lineY = (int) getY() + 2;
        for (String line : lines) {
            context.drawText(mc.textRenderer, Text.literal(line),
                    (int) getX() + 2, lineY, textColor, shadow);
            lineY += mc.textRenderer.fontHeight + 1;
        }

        // 更新大小
        int maxWidth = 0;
        for (String line : lines) {
            int width = mc.textRenderer.getWidth(line);
            if (width > maxWidth) maxWidth = width;
        }
        setSize(maxWidth + 4, lines.length * (mc.textRenderer.fontHeight + 1) + 4);
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