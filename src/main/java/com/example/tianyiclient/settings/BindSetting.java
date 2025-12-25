package com.example.tianyiclient.settings;

import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定设置 - 用于为模块设置独立的快捷键
 * 功能：
 * - 绑定模块到特定按键
 * - 支持按键冲突检测
 * - 提供按键名称显示
 * - 支持取消绑定（设置为0）
 *
 * 使用示例：
 * 飞行模块绑定到F键
 * 夜视模块绑定到N键
 * 杀戮光环绑定到K键
 */
public class BindSetting extends Setting<Integer> {
    private int key;

    /**
     * 创建按键绑定设置
     * @param name 设置名称，如"快捷键"
     * @param description 描述信息
     * @param defaultKey 默认按键代码，使用GLFW常量，如GLFW.GLFW_KEY_F
     */
    public BindSetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
        this.key = defaultKey;
    }

    /**
     * 简化构造函数
     */
    public BindSetting(String name, int defaultKey) {
        this(name, "", defaultKey);
    }

    /**
     * 无参构造函数，默认未绑定
     */
    public BindSetting() {
        this("快捷键", 0); // 0表示未绑定
    }

    @Override
    public Integer getValue() {
        return key;
    }

    @Override
    public void setValue(Integer key) {
        this.key = key;
    }

    /**
     * 设置按键
     */
    public void setKey(int key) {
        this.key = key;
    }

    /**
     * 获取按键代码
     */
    public int getKey() {
        return key;
    }

    /**
     * 获取按键的可读名称
     * 例如：GLFW.GLFW_KEY_F -> "F"
     */
    public String getKeyName() {
        if (key == 0) {
            return "未绑定";
        }

        // 将GLFW按键代码转换为可读名称
        String keyName = getKeyName(key);
        return keyName != null ? keyName : "键" + key;
    }

    /**
     * 检查是否已绑定按键
     */
    public boolean isBound() {
        return key != 0;
    }

    /**
     * 取消绑定（设置为0）
     */
    public void unbind() {
        this.key = 0;
    }

    /**
     * 检查按键是否被按下（用于模块触发）
     */
    public boolean isPressed() {
        // 这个方法需要在游戏循环中调用，这里只是框架
        // 实际实现需要在客户端tick事件中检查按键状态
        return isBound(); // 简化实现，实际需要接入输入系统
    }

    /**
     * 将GLFW按键代码转换为可读名称
     */
    private String getKeyName(int keyCode) {
        switch (keyCode) {
            // 字母键
            case GLFW.GLFW_KEY_A: return "A";
            case GLFW.GLFW_KEY_B: return "B";
            case GLFW.GLFW_KEY_C: return "C";
            case GLFW.GLFW_KEY_D: return "D";
            case GLFW.GLFW_KEY_E: return "E";
            case GLFW.GLFW_KEY_F: return "F";
            case GLFW.GLFW_KEY_G: return "G";
            case GLFW.GLFW_KEY_H: return "H";
            case GLFW.GLFW_KEY_I: return "I";
            case GLFW.GLFW_KEY_J: return "J";
            case GLFW.GLFW_KEY_K: return "K";
            case GLFW.GLFW_KEY_L: return "L";
            case GLFW.GLFW_KEY_M: return "M";
            case GLFW.GLFW_KEY_N: return "N";
            case GLFW.GLFW_KEY_O: return "O";
            case GLFW.GLFW_KEY_P: return "P";
            case GLFW.GLFW_KEY_Q: return "Q";
            case GLFW.GLFW_KEY_R: return "R";
            case GLFW.GLFW_KEY_S: return "S";
            case GLFW.GLFW_KEY_T: return "T";
            case GLFW.GLFW_KEY_U: return "U";
            case GLFW.GLFW_KEY_V: return "V";
            case GLFW.GLFW_KEY_W: return "W";
            case GLFW.GLFW_KEY_X: return "X";
            case GLFW.GLFW_KEY_Y: return "Y";
            case GLFW.GLFW_KEY_Z: return "Z";

            // 数字键
            case GLFW.GLFW_KEY_0: return "0";
            case GLFW.GLFW_KEY_1: return "1";
            case GLFW.GLFW_KEY_2: return "2";
            case GLFW.GLFW_KEY_3: return "3";
            case GLFW.GLFW_KEY_4: return "4";
            case GLFW.GLFW_KEY_5: return "5";
            case GLFW.GLFW_KEY_6: return "6";
            case GLFW.GLFW_KEY_7: return "7";
            case GLFW.GLFW_KEY_8: return "8";
            case GLFW.GLFW_KEY_9: return "9";

            // 功能键
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
            case GLFW.GLFW_KEY_F13: return "F13";
            case GLFW.GLFW_KEY_F14: return "F14";
            case GLFW.GLFW_KEY_F15: return "F15";
            case GLFW.GLFW_KEY_F16: return "F16";
            case GLFW.GLFW_KEY_F17: return "F17";
            case GLFW.GLFW_KEY_F18: return "F18";
            case GLFW.GLFW_KEY_F19: return "F19";
            case GLFW.GLFW_KEY_F20: return "F20";
            case GLFW.GLFW_KEY_F21: return "F21";
            case GLFW.GLFW_KEY_F22: return "F22";
            case GLFW.GLFW_KEY_F23: return "F23";
            case GLFW.GLFW_KEY_F24: return "F24";
            case GLFW.GLFW_KEY_F25: return "F25";

            // 特殊键
            case GLFW.GLFW_KEY_SPACE: return "空格";
            case GLFW.GLFW_KEY_ENTER: return "回车";
            case GLFW.GLFW_KEY_ESCAPE: return "ESC";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_BACKSPACE: return "退格";
            case GLFW.GLFW_KEY_INSERT: return "Insert";
            case GLFW.GLFW_KEY_DELETE: return "Delete";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "PageUp";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PageDown";

            // 方向键
            case GLFW.GLFW_KEY_UP: return "上箭头";
            case GLFW.GLFW_KEY_DOWN: return "下箭头";
            case GLFW.GLFW_KEY_LEFT: return "左箭头";
            case GLFW.GLFW_KEY_RIGHT: return "右箭头";

            // 修饰键
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "左Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "右Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "左Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "右Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT: return "左Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "右Alt";
            case GLFW.GLFW_KEY_LEFT_SUPER: return "左Win";
            case GLFW.GLFW_KEY_RIGHT_SUPER: return "右Win";

            default: return null;
        }
    }
}