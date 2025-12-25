package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.LightType;

/**
 * 透明风格游戏信息显示
 */
public class GameInfoElement extends HudElement {

    public GameInfoElement() {
        super("游戏信息", 10.0f, 150.0f);
        setSize(140, 45); // 稍微宽一点以容纳时间段
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.world == null || mc.player == null || !isVisible()) return;

        renderTransparentPanel(context);
    }

    /**
     * 渲染透明面板
     */
    private void renderTransparentPanel(DrawContext context) {
        int x = (int) getX();
        int y = (int) getY();

        // 显示三行信息
        int startY = y;

        // 1. 生物群系（第一行）
        String biome = getBiomeName();
        context.drawText(mc.textRenderer, "🌿" + biome, x, startY, 0x88FFFFFF, true);
        startY += 11;

        // 2. 游戏时间带时间段（第二行）
        String timeWithPeriod = getGameTimeWithPeriod();
        context.drawText(mc.textRenderer, "⏰" + timeWithPeriod, x, startY, 0x88FFFFFF, true);
        startY += 11;

        // 3. 亮度和天气（第三行）
        int lightLevel = getLightLevel();
        String lightIcon = getLightIcon(lightLevel);
        String weatherIcon = getWeatherIcon();

        String info = String.format("%s%d级 %s", lightIcon, lightLevel, weatherIcon);
        context.drawText(mc.textRenderer, info, x, startY, 0x88FFFFFF, true);

        // 自动调整大小
        int maxWidth = Math.max(
                mc.textRenderer.getWidth("🌿" + biome),
                Math.max(
                        mc.textRenderer.getWidth("⏰" + timeWithPeriod),
                        mc.textRenderer.getWidth(info)
                )
        );
        setSize(maxWidth + 5, 35);
    }

    // ========== 信息获取方法 ==========

    private String getBiomeName() {
        try {
            String biomeId = mc.world.getBiome(mc.player.getBlockPos()).getKey().toString();

            if (biomeId.contains("plains")) return "平原";
            if (biomeId.contains("forest")) return "森林";
            if (biomeId.contains("desert")) return "沙漠";
            if (biomeId.contains("mountains")) return "山脉";
            if (biomeId.contains("jungle")) return "丛林";
            if (biomeId.contains("swamp")) return "沼泽";
            if (biomeId.contains("ocean")) return "海洋";
            if (biomeId.contains("river")) return "河流";
            if (biomeId.contains("taiga")) return "针叶林";
            if (biomeId.contains("snowy")) return "雪原";
            if (biomeId.contains("beach")) return "海滩";
            if (biomeId.contains("mushroom")) return "蘑菇岛";
            if (biomeId.contains("nether")) return "下界";
            if (biomeId.contains("end")) return "末地";

            return "未知";
        } catch (Exception e) {
            return "未知";
        }
    }

    private String getGameTimeWithPeriod() {
        long time = mc.world.getTimeOfDay();
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) * 60 / 1000;

        String period;
        if (hours >= 5 && hours < 7) {
            period = "清晨";
        } else if (hours >= 7 && hours < 12) {
            period = "上午";
        } else if (hours >= 12 && hours < 18) {
            period = "下午";
            if (hours > 12) hours -= 12;
        } else if (hours >= 18 && hours < 21) {
            period = "傍晚";
            hours -= 12;
        } else {
            period = "夜晚";
            hours = hours > 12 ? hours - 12 : hours;
        }

        return String.format("%s%02d:%02d", period, hours, minutes);
    }

    private int getLightLevel() {
        try {
            return mc.world.getLightLevel(LightType.SKY, mc.player.getBlockPos());
        } catch (Exception e) {
            return 0;
        }
    }

    private String getLightIcon(int level) {
        if (level > 12) return "☀️";
        if (level > 8) return "🌤️";
        if (level > 4) return "⛅";
        return "🌙";
    }

    private String getWeatherIcon() {
        if (mc.world.isThundering()) return "⛈️";
        if (mc.world.isRaining()) return "🌧️";

        // 根据时间显示不同图标
        long time = mc.world.getTimeOfDay();
        boolean isNight = time > 13000 && time < 23000;
        return isNight ? "🌙" : "☀️";
    }
}