package com.example.tianyiclient.managers;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.utils.Rotation;
import net.minecraft.client.MinecraftClient;

public class RotationManager extends Manager {
    private static RotationManager instance;
    private MinecraftClient mc;

    public enum RotationMode {
        INSTANT,
        SMOOTH,
        STEPPED,
        SERVER_SIDE
    }

    private Rotation currentTarget;
    private RotationMode mode = RotationMode.SERVER_SIDE;
    private float smoothSpeed = 0.1f;
    private boolean isActive = false;

    private RotationManager() {
        super("RotationManager");
        this.mc = MinecraftClient.getInstance();
    }

    public static RotationManager getInstance() {
        if (instance == null) instance = new RotationManager();
        return instance;
    }

    @Override
    public void onInit() {
        TianyiClient.LOGGER.info("RotationManager 初始化");
        this.mc = MinecraftClient.getInstance();
        isActive = true;
    }

    public void onTick() {
        if (!isActive || mc.player == null || currentTarget == null) return;

        Rotation playerRotation = Rotation.fromPlayer();

        switch (mode) {
            case INSTANT:
                applyToPlayer(currentTarget);
                currentTarget = null;
                break;

            case SMOOTH:
                Rotation smoothed = playerRotation.lerp(currentTarget, smoothSpeed);
                applyToPlayer(smoothed);

                if (playerRotation.distance(currentTarget) < 1.0f) {
                    currentTarget = null;
                }
                break;

            case SERVER_SIDE:
                break;

            case STEPPED:
                float stepSize = 5.0f;
                Rotation stepped = stepTowards(playerRotation, currentTarget, stepSize);
                applyToPlayer(stepped);

                if (playerRotation.distance(currentTarget) < stepSize) {
                    currentTarget = null;
                }
                break;
        }
    }

    private Rotation stepTowards(Rotation from, Rotation to, float maxStep) {
        float[] diff = from.difference(to);
        float yawStep = Math.min(Math.abs(diff[0]), maxStep) * Math.signum(diff[0]);
        float pitchStep = Math.min(Math.abs(diff[1]), maxStep) * Math.signum(diff[1]);

        return new Rotation(from.getYaw() + yawStep, from.getPitch() + pitchStep);
    }

    public void setTargetRotation(Rotation rotation, RotationMode mode) {
        this.currentTarget = rotation;
        this.mode = mode;
        isActive = true;
    }

    public void setTargetRotation(Rotation rotation) {
        setTargetRotation(rotation, RotationMode.SERVER_SIDE);
    }

    public void setSmoothSpeed(float speed) {
        this.smoothSpeed = Math.max(0.01f, Math.min(speed, 1.0f));
    }

    public RotationMode getCurrentMode() {
        return mode;
    }

    public Rotation getCurrentTarget() {
        return currentTarget;
    }

    public boolean isRotating() {
        return currentTarget != null && isActive;
    }

    public void stopRotation() {
        currentTarget = null;
        isActive = false;
    }

    private void applyToPlayer(Rotation rotation) {
        if (mc.player != null) {
            mc.player.setYaw(rotation.getYaw());
            mc.player.setPitch(rotation.getPitch());
        }
    }

    @Override
    public void onShutdown() {
        TianyiClient.LOGGER.info("RotationManager 关闭");
        isActive = false;
        currentTarget = null;
    }
}