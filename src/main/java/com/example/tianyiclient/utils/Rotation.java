package com.example.tianyiclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Rotation {
    private final float yaw;
    private final float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public static Rotation fromPlayer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            return new Rotation(mc.player.getYaw(), mc.player.getPitch());
        }
        return new Rotation(0, 0);
    }

    public static Rotation lookAt(Vec3d from, Vec3d to) {
        Vec3d diff = to.subtract(from);
        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, horizontalDistance));

        return new Rotation((float) yaw, (float) pitch);
    }

    // 🔥 新增：lookAt(Entity) 方法重载
    public static Rotation lookAt(Entity entity) {
        if (entity == null) return new Rotation(0, 0);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return new Rotation(0, 0);

        // 获取玩家眼睛位置
        Vec3d eyePos = mc.player.getEyePos();
        // 获取实体中心位置
        Vec3d targetPos = entity.getBoundingBox().getCenter();

        return lookAt(eyePos, targetPos);
    }

    public Rotation lerp(Rotation target, float progress) {
        float yawDiff = target.getYaw() - this.yaw;
        float pitchDiff = target.getPitch() - this.pitch;

        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;

        return new Rotation(
                this.yaw + yawDiff * progress,
                this.pitch + pitchDiff * progress
        );
    }

    public float distance(Rotation other) {
        float yawDiff = Math.abs(other.getYaw() - this.yaw);
        float pitchDiff = Math.abs(other.getPitch() - this.pitch);

        while (yawDiff > 180) yawDiff = 360 - yawDiff;

        return (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }

    public float[] difference(Rotation other) {
        float yawDiff = other.getYaw() - this.yaw;
        float pitchDiff = other.getPitch() - this.pitch;

        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;

        return new float[]{yawDiff, pitchDiff};
    }

    @Override
    public String toString() {
        return String.format("Yaw: %.2f, Pitch: %.2f", yaw, pitch);
    }
}