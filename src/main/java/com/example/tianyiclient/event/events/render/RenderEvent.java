package com.example.tianyiclient.event.events.render;

import com.example.tianyiclient.event.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * 通用渲染事件基类
 * 包含渲染所需的基本信息
 */
public abstract class RenderEvent extends Event {
    protected final MatrixStack matrices;
    protected final float tickDelta;
    protected final double cameraX;
    protected final double cameraY;
    protected final double cameraZ;

    public RenderEvent(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
    }

    /**
     * 获取矩阵栈
     * @return MatrixStack对象
     */
    public MatrixStack getMatrices() {
        return matrices;
    }

    /**
     * 获取部分刻（用于插值）
     * @return tickDelta值
     */
    public float getTickDelta() {
        return tickDelta;
    }

    /**
     * 获取相机位置
     * @return 相机位置的Vec3d对象
     */
    public Vec3d getCameraPos() {
        return new Vec3d(cameraX, cameraY, cameraZ);
    }

    /**
     * 获取相机X坐标
     */
    public double getCameraX() {
        return cameraX;
    }

    /**
     * 获取相机Y坐标
     */
    public double getCameraY() {
        return cameraY;
    }

    /**
     * 获取相机Z坐标
     */
    public double getCameraZ() {
        return cameraZ;
    }

    /**
     * 将世界坐标转换为屏幕坐标
     * @param x 世界X坐标
     * @param y 世界Y坐标
     * @param z 世界Z坐标
     * @return 屏幕坐标[x, y]或null（如果不在屏幕上）
     */
    public double[] worldToScreen(double x, double y, double z) {
        // 需要根据具体情况实现
        // 这里提供一个占位实现
        return null;
    }
}

/**
 * PreRenderEvent - 渲染前事件
 */
class PreRenderEvent extends RenderEvent {
    public PreRenderEvent(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        super(matrices, tickDelta, cameraX, cameraY, cameraZ);
    }

    @Override
    public String toString() {
        return String.format("PreRenderEvent{camera=[%.1f, %.1f, %.1f]}", cameraX, cameraY, cameraZ);
    }
}

/**
 * PostRenderEvent - 渲染后事件
 */
class PostRenderEvent extends RenderEvent {
    public PostRenderEvent(MatrixStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        super(matrices, tickDelta, cameraX, cameraY, cameraZ);
    }

    @Override
    public String toString() {
        return String.format("PostRenderEvent{camera=[%.1f, %.1f, %.1f]}", cameraX, cameraY, cameraZ);
    }
}