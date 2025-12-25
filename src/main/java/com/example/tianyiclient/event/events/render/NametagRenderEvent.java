package com.example.tianyiclient.event.events.render;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.client.MinecraftClient; // 关键导入
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack; // 注意：这是1.21.8中DrawContext使用的类型

/**
 * 名字标签渲染事件
 * 用于自定义实体名字标签的渲染
 * 注意：这是一个纯客户端事件，仅在渲染时触发，服务器不可见。
 */
public class NametagRenderEvent extends Event implements Cancelable {
    protected final Entity entity;
    protected final Text text;
    protected final MatrixStack matrices; // 用于3D渲染
    protected final VertexConsumerProvider vertexConsumers;
    protected final int light;
    protected final float tickDelta;
    protected boolean cancelled = false;

    /**
     * 创建NametagRenderEvent
     * @param entity 实体对象
     * @param text 显示的文字
     * @param matrices 矩阵栈 (用于3D世界渲染)
     * @param vertexConsumers 顶点消费者
     * @param light 光照等级
     * @param tickDelta 部分刻
     */
    public NametagRenderEvent(Entity entity, Text text, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        this.entity = entity;
        this.text = text;
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
        this.light = light;
        this.tickDelta = tickDelta;
    }

    /**
     * 获取实体对象
     * @return Entity对象
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * 获取实体类型
     * @return 实体类型字符串
     */
    public String getEntityType() {
        return entity.getType().toString();
    }

    /**
     * 获取实体UUID
     * @return 实体的UUID
     */
    public String getEntityUuid() {
        return entity.getUuid().toString();
    }

    /**
     * 获取显示的文字
     * @return Text对象
     */
    public Text getText() {
        return text;
    }

    /**
     * 设置显示的文字（注意：原text字段为final，此方法仅为示例，实际需重新创建事件）
     * @param newText 新的Text对象
     * @return 一个新的NametagRenderEvent实例，包含修改后的文本
     */
    public NametagRenderEvent withNewText(Text newText) {
        return new NametagRenderEvent(this.entity, newText, this.matrices, this.vertexConsumers, this.light, this.tickDelta);
    }

    /**
     * 获取矩阵栈
     * @return MatrixStack对象 (用于3D世界渲染)
     */
    public MatrixStack getMatrices() {
        return matrices;
    }

    /**
     * 获取顶点消费者
     * @return VertexConsumerProvider对象
     */
    public VertexConsumerProvider getVertexConsumers() {
        return vertexConsumers;
    }

    /**
     * 获取光照等级
     * @return 光照等级
     */
    public int getLight() {
        return light;
    }

    /**
     * 获取部分刻
     * @return tickDelta值
     */
    public float getTickDelta() {
        return tickDelta;
    }

    /**
     * 获取实体位置
     * @return 实体位置的Vec3d
     */
    public net.minecraft.util.math.Vec3d getEntityPos() {
        return entity.getPos();
    }

    /**
     * 【已修复】获取实体与本地客户端的距离
     * 重要：此方法仅通过客户端API计算，不访问私有字段，完全在客户端执行，服务器无法检测。
     * @return 距离（方块），如果无法计算则返回0
     */
    public double getDistanceToPlayer() {
        // 正确方式：获取Minecraft客户端实例和玩家
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity == null) {
            return 0; // 客户端玩家不存在或实体无效
        }
        // 纯客户端计算，安全
        return entity.getPos().distanceTo(client.player.getPos());
    }

    /**
     * 检查是否为玩家实体
     * @return 如果是玩家返回true
     */
    public boolean isPlayer() {
        return entity.getType() == net.minecraft.entity.EntityType.PLAYER;
    }

    /**
     * 检查是否为生物实体
     * @return 如果是生物返回true
     */
    public boolean isLivingEntity() {
        return entity instanceof net.minecraft.entity.LivingEntity;
    }

    /**
     * 检查是否为物品实体
     * @return 如果是物品返回true
     */
    public boolean isItemEntity() {
        return entity instanceof net.minecraft.entity.ItemEntity;
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
        return String.format("NametagRenderEvent{entity=%s, text='%s', cancelled=%s}",
                entity.getType().getName().getString(),
                text.getString(),
                cancelled);
    }
}

/**
 * PreNametagRenderEvent - 名字标签渲染前事件
 */
class PreNametagRenderEvent extends NametagRenderEvent {
    public PreNametagRenderEvent(Entity entity, Text text, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        super(entity, text, matrices, vertexConsumers, light, tickDelta);
    }
}

/**
 * PostNametagRenderEvent - 名字标签渲染后事件
 */
class PostNametagRenderEvent extends NametagRenderEvent {
    public PostNametagRenderEvent(Entity entity, Text text, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        super(entity, text, matrices, vertexConsumers, light, tickDelta);
    }
}