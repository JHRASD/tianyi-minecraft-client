package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import org.lwjgl.glfw.GLFW;

/**
 * 键盘按键事件
 * 当键盘按键被按下、重复或释放时触发
 * 支持按键组合（Ctrl、Shift、Alt）
 */
public class KeyEvent extends Event implements Cancelable {
    /**
     * 按键状态
     */
    public enum State {
        /**
         * 按键按下
         */
        PRESS,

        /**
         * 按键重复（长按）
         */
        REPEAT,

        /**
         * 按键释放
         */
        RELEASE
    }

    private final int keyCode;
    private final int scanCode;
    private final State state;
    private final int modifiers;
    private final long windowHandle;
    private boolean cancelled = false;
    private long timestamp;

    /**
     * 创建KeyEvent
     * @param keyCode GLFW按键代码
     * @param scanCode 系统扫描码
     * @param action GLFW动作（PRESS=1, RELEASE=0, REPEAT=2）
     * @param modifiers 修饰键位掩码
     * @param windowHandle 窗口句柄
     */
    public KeyEvent(int keyCode, int scanCode, int action, int modifiers, long windowHandle) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.state = fromGLFWAction(action);
        this.modifiers = modifiers;
        this.windowHandle = windowHandle;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 将GLFW动作转换为State枚举
     */
    private static State fromGLFWAction(int action) {
        switch (action) {
            case GLFW.GLFW_PRESS:
                return State.PRESS;
            case GLFW.GLFW_RELEASE:
                return State.RELEASE;
            case GLFW.GLFW_REPEAT:
                return State.REPEAT;
            default:
                return State.RELEASE;
        }
    }

    /**
     * 获取按键代码（GLFW常量）
     * @return GLFW按键代码
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * 获取系统扫描码
     * @return 扫描码
     */
    public int getScanCode() {
        return scanCode;
    }

    /**
     * 获取按键状态
     * @return 按键状态
     */
    public State getState() {
        return state;
    }

    /**
     * 获取修饰键位掩码
     * @return 修饰键位掩码
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * 获取窗口句柄
     * @return GLFW窗口句柄
     */
    public long getWindowHandle() {
        return windowHandle;
    }

    /**
     * 获取事件时间戳
     * @return 事件发生的时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 检查是否为按下事件
     * @return 如果是按下返回true
     */
    public boolean isPress() {
        return state == State.PRESS;
    }

    /**
     * 检查是否为释放事件
     * @return 如果是释放返回true
     */
    public boolean isRelease() {
        return state == State.RELEASE;
    }

    /**
     * 检查是否为重复事件
     * @return 如果是重复返回true
     */
    public boolean isRepeat() {
        return state == State.REPEAT;
    }

    /**
     * 检查Ctrl键是否按下
     * @return 如果Ctrl按下返回true
     */
    public boolean isCtrlPressed() {
        return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    /**
     * 检查Shift键是否按下
     * @return 如果Shift按下返回true
     */
    public boolean isShiftPressed() {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    /**
     * 检查Alt键是否按下
     * @return 如果Alt按下返回true
     */
    public boolean isAltPressed() {
        return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
    }

    /**
     * 检查Super键（Windows键/Command键）是否按下
     * @return 如果Super按下返回true
     */
    public boolean isSuperPressed() {
        return (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
    }

    /**
     * 检查Caps Lock是否启用
     * @return 如果Caps Lock启用返回true
     */
    public boolean isCapsLockOn() {
        return (modifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0;
    }

    /**
     * 检查Num Lock是否启用
     * @return 如果Num Lock启用返回true
     */
    public boolean isNumLockOn() {
        return (modifiers & GLFW.GLFW_MOD_NUM_LOCK) != 0;
    }

    /**
     * 获取按键名称（如果有）
     * @return 按键名称，如"A"、"F1"、"SPACE"
     */
    public String getKeyName() {
        return GLFW.glfwGetKeyName(keyCode, scanCode);
    }

    // 在 KeyEvent.java 中添加这两个方法
    public int getKey() {
        return keyCode;
    }

    public int getAction() {
        switch (state) {
            case PRESS: return GLFW.GLFW_PRESS;
            case RELEASE: return GLFW.GLFW_RELEASE;
            case REPEAT: return GLFW.GLFW_REPEAT;
            default: return GLFW.GLFW_RELEASE;
        }
    }

    /**
     * 获取标准化的按键名称
     * @return 标准化的按键名称
     */
    public String getNormalizedKeyName() {
        String name = getKeyName();
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
        }

        // 特殊按键的处理
        switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_ESCAPE: return "ESCAPE";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_BACKSPACE: return "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT: return "INSERT";
            case GLFW.GLFW_KEY_DELETE: return "DELETE";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PAGE_UP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PAGE_DOWN";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
            default: return "KEY_" + keyCode;
        }
    }

    /**
     * 获取修饰键的字符串表示
     * @return 修饰键字符串，如"CTRL+SHIFT"
     */
    public String getModifiersString() {
        StringBuilder sb = new StringBuilder();

        if (isCtrlPressed()) sb.append("CTRL+");
        if (isShiftPressed()) sb.append("SHIFT+");
        if (isAltPressed()) sb.append("ALT+");
        if (isSuperPressed()) sb.append("SUPER+");

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 移除最后一个"+"
        }

        return sb.toString();
    }

    /**
     * 获取完整的按键组合字符串
     * @return 如"CTRL+SHIFT+A"
     */
    public String getFullKeyString() {
        String modifiers = getModifiersString();
        String key = getNormalizedKeyName();

        if (!modifiers.isEmpty()) {
            return modifiers + "+" + key;
        }
        return key;
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
        return String.format("KeyEvent{key=%s(%d), state=%s, modifiers=%s, cancelled=%s, window=%d}",
                getNormalizedKeyName(), keyCode, state,
                Integer.toBinaryString(modifiers), cancelled, windowHandle);
    }
}

