package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.text.DecimalFormat;

/**
 * 简洁版网络信息显示（TPS、延迟、服务器信息）
 */
public class NetworkInfoElement extends HudElement {

    private long lastUpdateTime = 0;
    private double estimatedTPS = 20.0;
    private int ping = -1;
    private final DecimalFormat df = new DecimalFormat("0.0");

    public NetworkInfoElement() {
        super("网络信息", 300.0f, 10.0f);
        setSize(120, 50);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isVisible() || mc.world == null) return;

        updateNetworkInfo();
        renderSimplePanel(context);
    }

    /**
     * 更新网络信息
     */
    private void updateNetworkInfo() {
        long currentTime = System.currentTimeMillis();

        // 每500ms更新一次
        if (currentTime - lastUpdateTime > 500) {
            // 估算TPS
            if (lastUpdateTime > 0) {
                long delta = currentTime - lastUpdateTime;
                estimatedTPS = Math.min(1000.0 / delta, 60.0);
            }

            // 获取延迟
            if (mc.getNetworkHandler() != null && mc.player != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                ping = entry != null ? entry.getLatency() : -1;
            }

            lastUpdateTime = currentTime;
        }
    }

    /**
     * 渲染简洁透明面板
     */
    private void renderSimplePanel(DrawContext context) {
        int x = (int) getX();
        int y = (int) getY();

        // 计算最大宽度
        int maxWidth = mc.textRenderer.getWidth("🌐 网络信息") + 10;

        // TPS信息
        String tpsText = String.format("⚙ TPS: %s", df.format(estimatedTPS));
        int tpsWidth = mc.textRenderer.getWidth(tpsText);
        if (tpsWidth > maxWidth) maxWidth = tpsWidth;

        // 延迟信息
        int pingWidth = 0;
        if (ping >= 0) {
            String pingText = String.format("📶 延迟: %dms", ping);
            pingWidth = mc.textRenderer.getWidth(pingText);
            if (pingWidth > maxWidth) maxWidth = pingWidth;
        }

        // 服务器信息
        String serverText = mc.isInSingleplayer() ? "🖥 单机游戏" : "🌐 多人游戏";
        int serverWidth = mc.textRenderer.getWidth(serverText);
        if (serverWidth > maxWidth) maxWidth = serverWidth;

        // 面板尺寸
        int panelWidth = maxWidth + 20;
        int panelHeight = 50;

        // 透明背景（极简风格）
        context.fill(x, y, x + panelWidth, y + panelHeight, 0x66000000);

        // 标题
        context.drawText(mc.textRenderer, "🌐 网络信息", x + 10, y + 5, 0xFF6C5CE7, true);

        // 分隔线
        context.fill(x + 5, y + 18, x + panelWidth - 5, y + 19, 0x886C5CE7);

        int textY = y + 24;

        // TPS（动态颜色）
        int tpsColor = getTPSColor(estimatedTPS);
        context.drawText(mc.textRenderer, tpsText, x + 10, textY, tpsColor, true);
        textY += 12;

        // 延迟（动态颜色）
        if (ping >= 0) {
            int pingColor = getPingColor(ping);
            String pingText = String.format("📶 延迟: %dms", ping);
            context.drawText(mc.textRenderer, pingText, x + 10, textY, pingColor, true);
        }

        // 更新大小
        setSize(panelWidth, panelHeight);
    }

    /**
     * 根据TPS获取颜色
     */
    private int getTPSColor(double tps) {
        if (tps >= 19.5) return 0xFF00FF96; // 绿色
        if (tps >= 15.0) return 0xFFFFD166; // 黄色
        return 0xFFFF6B6B; // 红色
    }

    /**
     * 根据延迟获取颜色
     */
    private int getPingColor(int ping) {
        if (ping < 50) return 0xFF00FF96;   // 绿色 - 极好
        if (ping < 100) return 0xFF55FF55;  // 浅绿 - 良好
        if (ping < 200) return 0xFFFFD166;  // 黄色 - 一般
        if (ping < 300) return 0xFFFFAA55;  // 橙色 - 较差
        return 0xFFFF6B6B;                  // 红色 - 极差
    }
}