package com.example.tianyiclient.utils.render;

import java.awt.*;

public class ColorUtil {
    public static Color fade(Color color, int index, int count) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float brightness = hsb[2];
        float step = 0.05f;
        if (count > 1) step = 0.1f / count;
        brightness += step * index;
        if (brightness > 1f) brightness = 1f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness));
    }

    public static Color interpolate(Color start, Color end, float progress) {
        int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        int alpha = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * progress);
        return new Color(red, green, blue, alpha);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) | (g << 8) | (b) | (a << 24);
    }

    public static int toARGB(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
}