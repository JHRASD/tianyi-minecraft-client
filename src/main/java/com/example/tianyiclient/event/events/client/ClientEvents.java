package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * 客户端事件工具类
 * 提供便捷的事件发布方法
 */
public class ClientEvents {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    private ClientEvents() {} // 工具类，防止实例化

    /**
     * 发布TickEvent
     * @param phase tick阶段
     * @param tickCount 游戏刻计数
     * @param partialTicks 部分刻
     */
    public static void postTickEvent(TickEvent.Phase phase, long tickCount, float partialTicks) {
        EventBus.getInstance().post(new TickEvent(phase, tickCount, partialTicks));
    }

    /**
     * 发布KeyEvent
     * @param keyCode 按键代码
     * @param scanCode 扫描码
     * @param action 动作
     * @param modifiers 修饰键
     * @param windowHandle 窗口句柄
     * @return 事件是否被取消
     */
    public static boolean postKeyEvent(int keyCode, int scanCode, int action, int modifiers, long windowHandle) {
        KeyEvent event = new KeyEvent(keyCode, scanCode, action, modifiers, windowHandle);
        return EventBus.getInstance().postAndCheckCancelled(event);
    }

    /**
     * 发布CharTypedEvent
     * @param character 输入的字符
     * @param modifiers 修饰键
     * @return 事件是否被取消
     */
    public static boolean postCharTypedEvent(char character, int modifiers) {
        CharTypedEvent event = new CharTypedEvent(character, modifiers);
        return EventBus.getInstance().postAndCheckCancelled(event);
    }

    /**
     * 发布MouseButtonEvent
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @param button 按钮
     * @param action 动作
     * @param modifiers 修饰键
     * @param windowHandle 窗口句柄
     * @return 事件是否被取消
     */
    public static boolean postMouseButtonEvent(double x, double y, int button, int action,
                                               int modifiers, long windowHandle) {
        MouseButtonEvent event = new MouseButtonEvent(x, y, button, action, modifiers, windowHandle);
        return EventBus.getInstance().postAndCheckCancelled(event);
    }

    /**
     * 发布MouseScrollEvent
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @param scrollX 水平滚动
     * @param scrollY 垂直滚动
     * @param windowHandle 窗口句柄
     * @return 事件是否被取消
     */
    public static boolean postMouseScrollEvent(double x, double y, double scrollX,
                                               double scrollY, long windowHandle) {
        MouseScrollEvent event = new MouseScrollEvent(x, y, scrollX, scrollY, windowHandle);
        return EventBus.getInstance().postAndCheckCancelled(event);
    }

    /**
     * 发布MouseMoveEvent
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @param deltaX X变化量
     * @param deltaY Y变化量
     * @param windowHandle 窗口句柄
     * @return 事件是否被取消
     */
    public static boolean postMouseMoveEvent(double x, double y, double deltaX,
                                             double deltaY, long windowHandle) {
        MouseMoveEvent event = new MouseMoveEvent(x, y, deltaX, deltaY, windowHandle);
        return EventBus.getInstance().postAndCheckCancelled(event);
    }

    /**
     * 发布GameJoinedEvent
     * @param world 世界对象
     * @param networkHandler 网络处理器
     * @param joinType 加入类型
     */
    public static void postGameJoinedEvent(net.minecraft.client.world.ClientWorld world,
                                           net.minecraft.client.network.ClientPlayNetworkHandler networkHandler,
                                           GameJoinedEvent.JoinType joinType) {
        EventBus.getInstance().post(new GameJoinedEvent(world, networkHandler, joinType));
    }

    /**
     * 发布GameLeftEvent
     * @param world 世界对象
     * @param networkHandler 网络处理器
     * @param reason 离开原因
     * @param nextScreen 下一个屏幕
     * @param playDuration 游玩时长
     */
    public static void postGameLeftEvent(net.minecraft.client.world.ClientWorld world,
                                         net.minecraft.client.network.ClientPlayNetworkHandler networkHandler,
                                         GameLeftEvent.LeaveReason reason,
                                         Screen nextScreen,
                                         long playDuration) {
        EventBus.getInstance().post(new GameLeftEvent(world, networkHandler, reason, nextScreen, playDuration));
    }

    /**
     * 判断当前是否在游戏中
     * @return 如果在游戏中返回true
     */
    public static boolean isInGame() {
        return MC.world != null && MC.player != null;
    }

    /**
     * 获取当前世界（安全检查）
     * @return 当前世界或null
     */
    public static net.minecraft.client.world.ClientWorld getCurrentWorld() {
        return isInGame() ? MC.world : null;
    }

    /**
     * 获取当前网络处理器（安全检查）
     * @return 网络处理器或null
     */
    public static net.minecraft.client.network.ClientPlayNetworkHandler getCurrentNetworkHandler() {
        return isInGame() ? MC.getNetworkHandler() : null;
    }
}