package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

/**
 * 加入游戏事件
 * 当玩家成功加入游戏世界时触发
 * 用于模块的初始化和世界相关设置
 */
public class GameJoinedEvent extends Event {
    private final ClientWorld world;
    private final ClientPlayNetworkHandler networkHandler;
    private final JoinType joinType;
    private final long joinTime;

    /**
     * 加入类型
     */
    public enum JoinType {
        /**
         * 单机游戏
         */
        SINGLEPLAYER,

        /**
         * 多人游戏 - 直接连接
         */
        MULTIPLAYER_DIRECT,

        /**
         * 多人游戏 - 通过服务器列表
         */
        MULTIPLAYER_SERVER_LIST,

        /**
         * 多人游戏 - Realm
         */
        MULTIPLAYER_REALM,

        /**
         * 局域网游戏
         */
        LAN,

        /**
         * 未知类型
         */
        UNKNOWN
    }

    /**
     * 创建GameJoinedEvent
     * @param world 客户端世界对象
     * @param networkHandler 网络处理器
     * @param joinType 加入类型
     */
    public GameJoinedEvent(ClientWorld world, ClientPlayNetworkHandler networkHandler, JoinType joinType) {
        this.world = world;
        this.networkHandler = networkHandler;
        this.joinType = joinType;
        this.joinTime = System.currentTimeMillis();
    }

    /**
     * 获取客户端世界
     * @return ClientWorld对象
     */
    public ClientWorld getWorld() {
        return world;
    }

    /**
     * 获取网络处理器
     * @return ClientPlayNetworkHandler对象
     */
    public ClientPlayNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    /**
     * 获取加入类型
     * @return 加入类型
     */
    public JoinType getJoinType() {
        return joinType;
    }

    /**
     * 获取加入时间戳
     * @return 加入时间（毫秒）
     */
    public long getJoinTime() {
        return joinTime;
    }

    /**
     * 检查是否为单机游戏
     * @return 如果是单机返回true
     */
    public boolean isSingleplayer() {
        return joinType == JoinType.SINGLEPLAYER;
    }

    /**
     * 检查是否为多人游戏
     * @return 如果是多人返回true
     */
    public boolean isMultiplayer() {
        return joinType == JoinType.MULTIPLAYER_DIRECT ||
                joinType == JoinType.MULTIPLAYER_SERVER_LIST ||
                joinType == JoinType.MULTIPLAYER_REALM ||
                joinType == JoinType.LAN;
    }

    /**
     * 检查是否为Realm游戏
     * @return 如果是Realm返回true
     */
    public boolean isRealm() {
        return joinType == JoinType.MULTIPLAYER_REALM;
    }

    /**
     * 检查是否为局域网游戏
     * @return 如果是局域网返回true
     */
    public boolean isLan() {
        return joinType == JoinType.LAN;
    }

    /**
     * 获取世界维度类型
     * @return 维度类型
     */
    public DimensionType getDimensionType() {
        return world.getDimension();
    }

    /**
     * 获取世界注册键
     * @return 世界注册键
     */
    public RegistryKey<World> getWorldRegistryKey() {
        return world.getRegistryKey();
    }

    /**
     * 获取世界时间
     * @return 世界时间（刻）
     */
    public long getWorldTime() {
        return world.getTime();
    }

    /**
     * 获取世界时间（天）
     * @return 世界天数
     */
    public long getWorldDay() {
        return world.getTimeOfDay() / 24000L;
    }

    /**
     * 获取服务器IP（如果是多人游戏）
     * @return 服务器IP地址，单机返回null
     */
    public String getServerIp() {
        if (networkHandler != null && networkHandler.getConnection() != null) {
            return networkHandler.getConnection().getAddress().toString();
        }
        return null;
    }

    /**
     * 获取服务器端口（如果是多人游戏）
     * @return 服务器端口，单机返回-1
     */
    public int getServerPort() {
        if (networkHandler != null && networkHandler.getConnection() != null) {
            String address = networkHandler.getConnection().getAddress().toString();
            if (address.contains(":")) {
                try {
                    return Integer.parseInt(address.split(":")[1]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * 获取加入游戏后的时间（毫秒）
     * @return 已加入的时间
     */
    public long getTimeSinceJoin() {
        return System.currentTimeMillis() - joinTime;
    }

    @Override
    public String toString() {
        return String.format("GameJoinedEvent{world=%s, joinType=%s, time=%d, server=%s:%d}",
                world != null ? world.getRegistryKey().getValue() : "null",
                joinType,
                joinTime,
                getServerIp(),
                getServerPort());
    }
}

/**
 * GameJoinAttemptEvent - 尝试加入游戏事件（可取消）
 * 在尝试加入游戏时触发，可以取消加入操作
 */
class GameJoinAttemptEvent extends Event implements Cancelable {
    private final String serverAddress;
    private final int serverPort;
    private final GameJoinedEvent.JoinType joinType;
    private boolean cancelled = false;
    private String cancelReason;

    public GameJoinAttemptEvent(String serverAddress, int serverPort, GameJoinedEvent.JoinType joinType) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.joinType = joinType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public GameJoinedEvent.JoinType getJoinType() {
        return joinType;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled && cancelReason == null) {
            cancelReason = "被模块取消";
        }
    }

    @Override
    public String toString() {
        return String.format("GameJoinAttemptEvent{server=%s:%d, type=%s, cancelled=%s, reason=%s}",
                serverAddress, serverPort, joinType, cancelled, cancelReason);
    }
}