package com.example.tianyiclient.utils.math;

import net.minecraft.util.math.MathHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtil {
    private static final Random random = new Random();

    public static double random(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static float random(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    public static int random(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double roundToStep(double value, double step) {
        if (step <= 0) return value;
        return Math.round(value / step) * step;
    }

    public static double interpolate(double current, double target, double speed) {
        return current + (target - current) * speed;
    }

    public static float interpolate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public static double normalizeAngle(double angle) {
        angle %= 360.0;
        if (angle <= -180) angle += 360;
        if (angle > 180) angle -= 360;
        return angle;
    }

    public static float normalizeAngle(float angle) {
        angle %= 360f;
        if (angle <= -180) angle += 360;
        if (angle > 180) angle -= 360;
        return angle;
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz));
    }

    public static double getDistanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return dx * dx + dy * dy + dz * dz;
    }
}