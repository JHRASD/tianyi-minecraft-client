package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import org.lwjgl.glfw.GLFW;

/**
 * 鼠标事件基类
 */
public abstract class MouseEvent extends Event implements Cancelable {
    protected double x;
    protected double y;
    protected int button;
    protected int action;
    protected int modifiers;
    protected long windowHandle;
    protected boolean cancelled = false;

    public MouseEvent(double x, double y, int button, int action, int modifiers, long windowHandle) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.action = action;
        this.modifiers = modifiers;
        this.windowHandle = windowHandle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean isPress() {
        return action == GLFW.GLFW_PRESS;
    }

    public boolean isRelease() {
        return action == GLFW.GLFW_RELEASE;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

/**
 * MouseButtonEvent - 鼠标按钮事件
 */
class MouseButtonEvent extends MouseEvent {
    public MouseButtonEvent(double x, double y, int button, int action, int modifiers, long windowHandle) {
        super(x, y, button, action, modifiers, windowHandle);
    }

    /**
     * 检查是否为左键
     */
    public boolean isLeftButton() {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }

    /**
     * 检查是否为右键
     */
    public boolean isRightButton() {
        return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    /**
     * 检查是否为中键
     */
    public boolean isMiddleButton() {
        return button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    }

    /**
     * 获取按钮名称
     */
    public String getButtonName() {
        switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT: return "LEFT";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT: return "RIGHT";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE: return "MIDDLE";
            default: return "BUTTON_" + button;
        }
    }

    @Override
    public String toString() {
        return String.format("MouseButtonEvent{x=%.1f, y=%.1f, button=%s, action=%s, cancelled=%s}",
                x, y, getButtonName(),
                action == GLFW.GLFW_PRESS ? "PRESS" : "RELEASE",
                cancelled);
    }
}

/**
 * MouseScrollEvent - 鼠标滚轮事件
 */
class MouseScrollEvent extends MouseEvent {
    private final double scrollX;
    private final double scrollY;

    public MouseScrollEvent(double x, double y, double scrollX, double scrollY, long windowHandle) {
        super(x, y, -1, -1, 0, windowHandle);
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    /**
     * 检查是否为垂直滚动
     */
    public boolean isVerticalScroll() {
        return Math.abs(scrollY) > Math.abs(scrollX);
    }

    /**
     * 检查是否为水平滚动
     */
    public boolean isHorizontalScroll() {
        return Math.abs(scrollX) > Math.abs(scrollY);
    }

    /**
     * 获取主要滚动方向的值
     */
    public double getMainScrollValue() {
        return isVerticalScroll() ? scrollY : scrollX;
    }

    @Override
    public String toString() {
        return String.format("MouseScrollEvent{x=%.1f, y=%.1f, scrollX=%.2f, scrollY=%.2f, cancelled=%s}",
                x, y, scrollX, scrollY, cancelled);
    }
}

/**
 * MouseMoveEvent - 鼠标移动事件
 */
class MouseMoveEvent extends MouseEvent {
    private final double deltaX;
    private final double deltaY;

    public MouseMoveEvent(double x, double y, double deltaX, double deltaY, long windowHandle) {
        super(x, y, -1, -1, 0, windowHandle);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    /**
     * 获取移动距离
     */
    public double getDistance() {
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * 获取移动角度（弧度）
     */
    public double getAngle() {
        return Math.atan2(deltaY, deltaX);
    }

    @Override
    public String toString() {
        return String.format("MouseMoveEvent{x=%.1f, y=%.1f, deltaX=%.2f, deltaY=%.2f, cancelled=%s}",
                x, y, deltaX, deltaY, cancelled);
    }
}

/**
 * MouseCursorEvent - 鼠标光标事件（进入/离开窗口）
 */
class MouseCursorEvent extends MouseEvent {
    public enum CursorAction {
        ENTER,
        LEAVE
    }

    private final CursorAction cursorAction;

    public MouseCursorEvent(double x, double y, CursorAction action, long windowHandle) {
        super(x, y, -1, -1, 0, windowHandle);
        this.cursorAction = action;
    }

    public CursorAction getCursorAction() {
        return cursorAction;
    }

    public boolean isEnter() {
        return cursorAction == CursorAction.ENTER;
    }

    public boolean isLeave() {
        return cursorAction == CursorAction.LEAVE;
    }

    @Override
    public String toString() {
        return String.format("MouseCursorEvent{x=%.1f, y=%.1f, action=%s, cancelled=%s}",
                x, y, cursorAction, cancelled);
    }
}