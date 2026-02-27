package com.example.tianyiclient.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * 绘制实心矩形
     */
    public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    /**
     * 绘制矩形边框（使用多个实心矩形模拟）
     */
    public static void drawOutline(DrawContext context, int x, int y, int width, int height, int color, int lineWidth) {
        // 上边
        context.fill(x, y, x + width, y + lineWidth, color);
        // 下边
        context.fill(x, y + height - lineWidth, x + width, y + height, color);
        // 左边
        context.fill(x, y + lineWidth, x + lineWidth, y + height - lineWidth, color);
        // 右边
        context.fill(x + width - lineWidth, y + lineWidth, x + width, y + height - lineWidth, color);
    }

    /**
     * 绘制垂直渐变矩形（使用 DrawContext 的内置方法）
     */
    public static void drawGradientRect(DrawContext context, int x, int y, int width, int height, int startColor, int endColor) {
        context.fillGradient(x, y, x + width, y + height, startColor, endColor);
    }

    /**
     * 绘制居中文字
     */
    public static void drawCenteredString(DrawContext context, String text, int x, int y, int color) {
        context.drawText(mc.textRenderer, text,
                x - mc.textRenderer.getWidth(text) / 2,
                y, color, true);
    }

    /**
     * 绘制文字（带阴影）
     */
    public static void drawString(DrawContext context, String text, int x, int y, int color) {
        context.drawText(mc.textRenderer, text, x, y, color, true);
    }

    /**
     * 绘制带背景的文字
     */
    public static void drawStringWithBackground(DrawContext context, String text, int x, int y, int textColor, int bgColor) {
        int width = mc.textRenderer.getWidth(text);
        context.fill(x - 2, y - 2, x + width + 2, y + mc.textRenderer.fontHeight + 2, bgColor);
        context.drawText(mc.textRenderer, text, x, y, textColor, true);
    }

    /**
     * 绘制简单线条（使用 DrawContext 的 drawHorizontalLine / drawVerticalLine，如果存在）
     * 1.21.8 中 DrawContext 可能没有直接画线的方法，所以用 fill 模拟
     */
    public static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color, int width) {
        if (x1 == x2) { // 垂直线
            context.fill(x1 - width/2, y1, x1 + width/2 + 1, y2, color);
        } else if (y1 == y2) { // 水平线
            context.fill(x1, y1 - width/2, x2, y1 + width/2 + 1, color);
        } else {
            // 斜线暂时无法用简单 fill 实现，可留空或使用更复杂的方法（这里省略）
        }
    }

    /**
     * 绘制圆形（近似，使用多个矩形？暂时无法简单实现，留空）
     * 需要时可通过绘制多个点或使用纹理，但为了编译通过，暂不实现
     */
    public static void drawCircle(MatrixStack matrices, float centerX, float centerY, float radius, int color) {
        // 在 1.21.8 中绘制圆形需要底层渲染，此处留空以避免编译错误
        // 如果需要圆形，可以考虑使用 DrawContext 的 drawTexture 配合圆形纹理
    }
}