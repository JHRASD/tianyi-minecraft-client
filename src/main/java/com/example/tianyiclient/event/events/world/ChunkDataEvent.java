package com.example.tianyiclient.event.events.world;

import com.example.tianyiclient.event.Event;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

/**
 * 区块数据事件
 * 当区块被加载、卸载或更新时触发
 */
public class ChunkDataEvent extends Event {
    private final ChunkPos chunkPos;
    private final WorldChunk chunk;
    private final EventType eventType;

    /**
     * 事件类型
     */
    public enum EventType {
        /**
         * 区块加载
         */
        CHUNK_LOAD,

        /**
         * 区块卸载
         */
        CHUNK_UNLOAD,

        /**
         * 区块数据更新
         */
        CHUNK_UPDATE,

        /**
         * 区块光照更新
         */
        LIGHT_UPDATE
    }

    public ChunkDataEvent(ChunkPos chunkPos, WorldChunk chunk, EventType eventType) {
        this.chunkPos = chunkPos;
        this.chunk = chunk;
        this.eventType = eventType;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public WorldChunk getChunk() {
        return chunk;
    }

    public EventType getEventType() {
        return eventType;
    }

    /**
     * 获取区块X坐标
     */
    public int getChunkX() {
        return chunkPos.x;
    }

    /**
     * 获取区块Z坐标
     */
    public int getChunkZ() {
        return chunkPos.z;
    }

    /**
     * 获取区块内的世界X坐标范围
     */
    public int[] getWorldXRange() {
        return new int[]{chunkPos.getStartX(), chunkPos.getEndX()};
    }

    /**
     * 获取区块内的世界Z坐标范围
     */
    public int[] getWorldZRange() {
        return new int[]{chunkPos.getStartZ(), chunkPos.getEndZ()};
    }

    /**
     * 检查是否为加载事件
     */
    public boolean isLoadEvent() {
        return eventType == EventType.CHUNK_LOAD;
    }

    /**
     * 检查是否为卸载事件
     */
    public boolean isUnloadEvent() {
        return eventType == EventType.CHUNK_UNLOAD;
    }

    /**
     * 获取区块是否为空（无方块）
     */
    public boolean isEmpty() {
        return chunk.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("ChunkDataEvent{pos=[%d,%d], type=%s}",
                chunkPos.x, chunkPos.z, eventType);
    }
}