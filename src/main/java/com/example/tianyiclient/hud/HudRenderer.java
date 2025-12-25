package com.example.tianyiclient.hud;

import com.example.tianyiclient.TianyiClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * 简化的HUD渲染器 - 现在只负责渲染HudElement系统
 */
public class HudRenderer {
    private static final HudRenderer INSTANCE = new HudRenderer();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // 动画（可选保留）
    private long lastRenderTime = 0;

    public static HudRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * 主渲染方法 - 现在只调用HudManager
     */
    public void render(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null || context == null || mc.world == null) {
            return;
        }

        // 检查HUD模块是否启用
        com.example.tianyiclient.modules.render.HUD hudModule =
                TianyiClient.getInstance().getHudModule();
        if (hudModule != null && !hudModule.isEnabled()) {
            return;
        }

        // 更新动画（可选）
        updateAnimations();

        // 渲染所有HUD元素（通过HudManager）
        renderNewHUD(context, tickDelta);
    }

    /**
     * 渲染HudElement系统
     */
    private void renderNewHUD(DrawContext context, float tickDelta) {
        HudManager hudManager = TianyiClient.getInstance().getHudManager();
        if (hudManager != null) {
            try {
                hudManager.renderHud(context, tickDelta);
            } catch (Exception e) {
                System.out.println("[HUD] HudManager渲染出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新动画（可选保留）
     */
    private void updateAnimations() {
        lastRenderTime = System.currentTimeMillis();
    }

    /**
     * 静态绘制方法（兼容旧代码）
     */
    public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    /**
     * 获取上次渲染时间（供其他元素使用）
     */
    public long getLastRenderTime() {
        return lastRenderTime;
    }
}