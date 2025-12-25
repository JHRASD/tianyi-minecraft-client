package com.example.tianyiclient.event.events.world;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

/**
 * 方块更新事件
 * 当世界中的方块状态发生变化时触发
 */
public class BlockUpdateEvent extends Event implements Cancelable {
    private final BlockPos pos;
    private final BlockState oldState;
    private final BlockState newState;
    private final UpdateReason reason;
    private boolean cancelled = false;

    /**
     * 方块更新原因
     */
    public enum UpdateReason {
        /**
         * 方块被放置
         */
        BLOCK_PLACED,

        /**
         * 方块被破坏
         */
        BLOCK_BROKEN,

        /**
         * 方块状态变化（如红石激活）
         */
        STATE_CHANGED,

        /**
         * 自然变化（如植物生长）
         */
        NATURAL,

        /**
         * 活塞推动
         */
        PISTON,

        /**
         * 流体更新
         */
        FLUID,

        /**
         * 其他原因
         */
        OTHER
    }

    public BlockUpdateEvent(BlockPos pos, BlockState oldState, BlockState newState, UpdateReason reason) {
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
        this.reason = reason;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getOldState() {
        return oldState;
    }

    public BlockState getNewState() {
        return newState;
    }

    public UpdateReason getReason() {
        return reason;
    }

    /**
     * 获取方块X坐标
     */
    public int getX() {
        return pos.getX();
    }

    /**
     * 获取方块Y坐标
     */
    public int getY() {
        return pos.getY();
    }

    /**
     * 获取方块Z坐标
     */
    public int getZ() {
        return pos.getZ();
    }

    /**
     * 检查是否为空气方块更新
     */
    public boolean isAirUpdate() {
        return newState.isAir();
    }

    /**
     * 检查是否为液体方块更新
     */
    public boolean isLiquidUpdate() {
        return newState.getFluidState().isEmpty();
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
        return String.format("BlockUpdateEvent{pos=[%d,%d,%d], old=%s, new=%s, reason=%s}",
                pos.getX(), pos.getY(), pos.getZ(),
                oldState.getBlock().getName().getString(),
                newState.getBlock().getName().getString(),
                reason);
    }
}