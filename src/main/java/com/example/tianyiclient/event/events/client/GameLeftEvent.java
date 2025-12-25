package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;  // 添加这行导入
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;

/**
 * 离开游戏事件
 * 当玩家离开游戏世界时触发
 * 用于模块的清理和状态重置
 */
public class GameLeftEvent extends Event {
    private final ClientWorld world;
    private final ClientPlayNetworkHandler networkHandler;
    private final LeaveReason reason;
    private final Screen nextScreen;
    private final long leaveTime;
    private final long playDuration;

    /**
     * 离开原因
     */
    public enum LeaveReason {
        /**
         * 正常断开连接
         */
        DISCONNECT,

        /**
         * 断开连接并返回主菜单
         */
        DISCONNECT_TO_MENU,

        /**
         * 断开连接并退出游戏
         */
        DISCONNECT_TO_EXIT,

        /**
         * 切换到其他世界（单机切换存档）
         */
        SWITCH_WORLD,

        /**
         * 服务器关闭/踢出
         */
        SERVER_CLOSED,

        /**
         * 被服务器踢出
         */
        KICKED,

        /**
         * 被服务器封禁
         */
        BANNED,

        /**
         * 连接超时
         */
        TIMEOUT,

        /**
         * 未知原因
         */
        UNKNOWN
    }

    /**
     * 创建GameLeftEvent
     * @param world 离开的世界（可能为null）
     * @param networkHandler 网络处理器（可能为null）
     * @param reason 离开原因
     * @param nextScreen 下一个屏幕（可能为null）
     * @param playDuration 游玩时长（毫秒）
     */
    public GameLeftEvent(ClientWorld world, ClientPlayNetworkHandler networkHandler,
                         LeaveReason reason, Screen nextScreen, long playDuration) {
        this.world = world;
        this.networkHandler = networkHandler;
        this.reason = reason;
        this.nextScreen = nextScreen;
        this.leaveTime = System.currentTimeMillis();
        this.playDuration = playDuration;
    }

    /**
     * 简化构造函数
     */
    public GameLeftEvent(LeaveReason reason, long playDuration) {
        this(null, null, reason, null, playDuration);
    }

    /**
     * 获取离开的世界（可能为null）
     * @return ClientWorld对象或null
     */
    public ClientWorld getWorld() {
        return world;
    }

    /**
     * 获取网络处理器（可能为null）
     * @return ClientPlayNetworkHandler对象或null
     */
    public ClientPlayNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    /**
     * 获取离开原因
     * @return 离开原因
     */
    public LeaveReason getReason() {
        return reason;
    }

    /**
     * 获取下一个屏幕
     * @return 下一个Screen对象或null
     */
    public Screen getNextScreen() {
        return nextScreen;
    }

    /**
     * 获取离开时间戳
     * @return 离开时间（毫秒）
     */
    public long getLeaveTime() {
        return leaveTime;
    }

    /**
     * 获取游玩时长
     * @return 游玩时长（毫秒）
     */
    public long getPlayDuration() {
        return playDuration;
    }

    /**
     * 检查是否为正常断开
     * @return 如果是正常断开返回true
     */
    public boolean isNormalDisconnect() {
        return reason == LeaveReason.DISCONNECT ||
                reason == LeaveReason.DISCONNECT_TO_MENU ||
                reason == LeaveReason.DISCONNECT_TO_EXIT;
    }

    /**
     * 检查是否为异常断开
     * @return 如果是异常断开返回true
     */
    public boolean isAbnormalDisconnect() {
        return reason == LeaveReason.SERVER_CLOSED ||
                reason == LeaveReason.KICKED ||
                reason == LeaveReason.BANNED ||
                reason == LeaveReason.TIMEOUT;
    }

    /**
     * 检查是否被踢出
     * @return 如果被踢出返回true
     */
    public boolean isKicked() {
        return reason == LeaveReason.KICKED;
    }

    /**
     * 检查是否被封禁
     * @return 如果被封禁返回true
     */
    public boolean isBanned() {
        return reason == LeaveReason.BANNED;
    }

    /**
     * 检查是否返回主菜单
     * @return 如果返回主菜单返回true
     */
    public boolean isReturningToMenu() {
        return reason == LeaveReason.DISCONNECT_TO_MENU;
    }

    /**
     * 检查是否退出游戏
     * @return 如果退出游戏返回true
     */
    public boolean isExitingGame() {
        return reason == LeaveReason.DISCONNECT_TO_EXIT;
    }

    /**
     * 获取游玩时长（格式化）
     * @return 格式化的游玩时长，如"1小时23分45秒"
     */
    public String getFormattedPlayDuration() {
        long seconds = playDuration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%d小时%02d分%02d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%02d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 获取踢出/封禁消息（如果有）
     * @return 踢出消息或null
     */
    public String getKickMessage() {
        if (networkHandler != null && networkHandler.getConnection() != null) {
            // 这里需要根据实际情况获取踢出消息
            // 通常可以从网络连接或断开屏幕获取
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("GameLeftEvent{reason=%s, duration=%s, world=%s}",
                reason, getFormattedPlayDuration(),
                world != null ? world.getRegistryKey().getValue() : "null");
    }
}

/**
 * BeforeGameLeftEvent - 离开游戏前事件（可取消）
 * 在离开游戏前触发，可以取消离开操作
 */
class BeforeGameLeftEvent extends Event implements Cancelable {
    private final GameLeftEvent.LeaveReason reason;  // 这里直接使用 LeaveReason，不加前缀
    private final Screen nextScreen;
    private boolean cancelled = false;
    private String cancelReason;

    public BeforeGameLeftEvent(GameLeftEvent.LeaveReason reason, Screen nextScreen) {
        this.reason = reason;
        this.nextScreen = nextScreen;
    }

    public GameLeftEvent.LeaveReason getReason() {
        return reason;
    }

    public Screen getNextScreen() {
        return nextScreen;
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
        return String.format("BeforeGameLeftEvent{reason=%s, nextScreen=%s, cancelled=%s, reason=%s}",
                reason, nextScreen != null ? nextScreen.getClass().getSimpleName() : "null",
                cancelled, cancelReason);
    }
}