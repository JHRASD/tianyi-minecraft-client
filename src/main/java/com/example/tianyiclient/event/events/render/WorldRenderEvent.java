package com.example.tianyiclient.event.events.render;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 世界渲染事件
 * 用于3D世界的渲染（ESP、方框、线条等）
 */
public class WorldRenderEvent extends RenderEvent implements Cancelable {
    /**
     * 渲染阶段
     */
    public enum Phase {
        /**
         * 天空渲染前
         */
        PRE_SKY,

        /**
         * 天空渲染后
         */
        POST_SKY,

        /**
         * 透明方块渲染前
         */
        PRE_TRANSPARENT,

        /**
         * 透明方块渲染后
         */
        POST_TRANSPARENT,

        /**
         * 实体渲染前
         */
        PRE_ENTITIES,

        /**
         * 实体渲染后
         */
        POST_ENTITIES,

        /**
         * 方块实体渲染前
         */
        PRE_BLOCK_ENTITIES,

        /**
         * 方块实体渲染后
         */
        POST_BLOCK_ENTITIES,

        /**
         * 全部渲染完成后
         */
        FINAL
    }

    protected final Camera camera;
    protected final Matrix4f projectionMatrix;
    protected final Phase phase;
    protected boolean cancelled = false;

    /**
     * 创建WorldRenderEvent
     * @param matrices 矩阵栈
     * @param tickDelta 部分刻
     * @param cameraX 相机X坐标
     * @param cameraY 相机Y坐标
     * @param cameraZ 相机Z坐标
     * @param camera 相机对象
     * @param projectionMatrix 投影矩阵
     * @param phase 渲染阶段
     */
    public WorldRenderEvent(MatrixStack matrices, float tickDelta,
                            double cameraX, double cameraY, double cameraZ,
                            Camera camera, Matrix4f projectionMatrix, Phase phase) {
        super(matrices, tickDelta, cameraX, cameraY, cameraZ);
        this.camera = camera;
        this.projectionMatrix = projectionMatrix;
        this.phase = phase;
    }

    /**
     * 获取相机对象
     * @return Camera对象
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 获取投影矩阵
     * @return Matrix4f投影矩阵
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * 获取渲染阶段
     * @return 渲染阶段
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * 获取相机的朝向向量
     * @return 相机朝向的Vec3d
     */
    public Vec3d getCameraDirection() {
        return camera.getPos();
    }

    /**
     * 获取相机的偏航角（yaw）
     * @return 偏航角度（度）
     */
    public float getCameraYaw() {
        return camera.getYaw();
    }

    /**
     * 获取相机的俯仰角（pitch）
     * @return 俯仰角度（度）
     */
    public float getCameraPitch() {
        return camera.getPitch();
    }

    /**
     * 检查是否为天空渲染前阶段
     */
    public boolean isPreSky() {
        return phase == Phase.PRE_SKY;
    }

    /**
     * 检查是否为天空渲染后阶段
     */
    public boolean isPostSky() {
        return phase == Phase.POST_SKY;
    }

    /**
     * 检查是否为实体渲染前阶段
     */
    public boolean isPreEntities() {
        return phase == Phase.PRE_ENTITIES;
    }

    /**
     * 检查是否为实体渲染后阶段
     */
    public boolean isPostEntities() {
        return phase == Phase.POST_ENTITIES;
    }

    /**
     * 检查是否为最终阶段
     */
    public boolean isFinal() {
        return phase == Phase.FINAL;
    }

    /**
     * 将世界坐标转换为屏幕坐标（使用投影矩阵）
     * @param worldX 世界X坐标
     * @param worldY 世界Y坐标
     * @param worldZ 世界Z坐标
     * @return 屏幕坐标[x, y, z]或null（如果在屏幕外）
     */
    public float[] worldToScreen(float worldX, float worldY, float worldZ) {
        // 获取Minecraft客户端实例以访问窗口尺寸
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return null;
        }

        // 动态获取当前窗口的宽度和高度
        int screenWidth = client.getWindow().getWidth();
        int screenHeight = client.getWindow().getHeight();

        // 转换为相机相对坐标
        float x = (float) (worldX - cameraX);
        float y = (float) (worldY - cameraY);
        float z = (float) (worldZ - cameraZ);

        // 创建齐次坐标
        org.joml.Vector4f pos = new org.joml.Vector4f(x, y, z, 1.0f);

        // 应用模型视图矩阵
        Matrix4f modelViewMatrix = new Matrix4f(matrices.peek().getPositionMatrix());
        pos.mul(modelViewMatrix);

        // 应用投影矩阵
        pos.mul(projectionMatrix);

        // 透视除法
        if (pos.w() == 0.0f) return null;

        pos.x /= pos.w();
        pos.y /= pos.w();
        pos.z /= pos.w();

        // 转换为屏幕坐标
        float screenX = (pos.x + 1.0f) * screenWidth / 2.0f;
        float screenY = (1.0f - pos.y) * screenHeight / 2.0f; // Y轴翻转

        // 检查是否在视锥体内
        if (pos.x < -1.0f || pos.x > 1.0f ||
                pos.y < -1.0f || pos.y > 1.0f ||
                pos.z < -1.0f || pos.z > 1.0f) {
            return null; // 在屏幕外
        }

        return new float[]{screenX, screenY, pos.z};
    }
    /**
     * 绘制3D线条的便捷方法
     * @param start 起点坐标
     * @param end 终点坐标
     * @param color 颜色（ARGB）
     * @param lineWidth 线宽
     */
    public void drawLine3D(Vec3d start, Vec3d end, int color, float lineWidth) {
        // 这里需要实现实际的3D线条渲染
        // 通常使用Tessellator或BufferBuilder
    }

    /**
     * 绘制3D方框的便捷方法
     * @param min 最小点坐标
     * @param max 最大点坐标
     * @param color 颜色（ARGB）
     * @param lineWidth 线宽
     */
    public void drawBox3D(Vec3d min, Vec3d max, int color, float lineWidth) {
        // 绘制方框的12条边
        Vec3d[] vertices = new Vec3d[]{
                new Vec3d(min.x, min.y, min.z), new Vec3d(max.x, min.y, min.z),
                new Vec3d(max.x, min.y, max.z), new Vec3d(min.x, min.y, max.z),
                new Vec3d(min.x, max.y, min.z), new Vec3d(max.x, max.y, min.z),
                new Vec3d(max.x, max.y, max.z), new Vec3d(min.x, max.y, max.z)
        };

        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // 底面
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // 顶面
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // 侧面
        };

        for (int[] edge : edges) {
            drawLine3D(vertices[edge[0]], vertices[edge[1]], color, lineWidth);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return String.format("WorldRenderEvent{phase=%s, camera=[%.1f, %.1f, %.1f], cancelled=%s}",
                phase, cameraX, cameraY, cameraZ, cancelled);
    }
}

/**
 * WorldRenderPostEvent - 世界渲染后事件（简化的）
 */
class WorldRenderPostEvent extends WorldRenderEvent {
    public WorldRenderPostEvent(MatrixStack matrices, float tickDelta,
                                double cameraX, double cameraY, double cameraZ,
                                Camera camera, Matrix4f projectionMatrix) {
        super(matrices, tickDelta, cameraX, cameraY, cameraZ,
                camera, projectionMatrix, Phase.FINAL);
    }
}