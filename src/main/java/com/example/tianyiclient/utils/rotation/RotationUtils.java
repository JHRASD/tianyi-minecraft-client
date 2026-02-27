package com.example.tianyiclient.utils.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {
    public static Rotation getRotationTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        return getRotation(dx, dy, dz);
    }

    public static Rotation getRotationToEntity(Entity from, Entity target) {
        Vec3d fromVec = from.getEyePos();
        Vec3d targetVec = target.getBoundingBox().getCenter();
        return getRotationTo(fromVec, targetVec);
    }

    public static Rotation getRotation(double dx, double dy, double dz) {
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new Rotation(yaw, pitch);
    }

    public static float getYawDifference(float currentYaw, float targetYaw) {
        float diff = targetYaw - currentYaw;
        diff = (diff + 180) % 360 - 180;
        return diff;
    }

    public static float getPitchDifference(float currentPitch, float targetPitch) {
        return targetPitch - currentPitch;
    }

    public static Rotation normalize(Rotation rotation) {
        float yaw = rotation.getYaw() % 360f;
        if (yaw <= -180) yaw += 360;
        if (yaw > 180) yaw -= 360;
        float pitch = MathHelper.clamp(rotation.getPitch(), -90f, 90f);
        return new Rotation(yaw, pitch);
    }

    public static class Rotation {
        private float yaw;
        private float pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public void setPitch(float pitch) { this.pitch = pitch; }

        @Override
        public String toString() {
            return String.format("Rotation(yaw=%.2f, pitch=%.2f)", yaw, pitch);
        }
    }
}