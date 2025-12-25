package com.example.tianyiclient.settings;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.managers.KeybindManager;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
    private Module boundModule;
    private boolean isKeyDown = false;
    private boolean isListening = false; // 新增：监听状态

    // 使用模块构造
    public KeybindSetting(String name, String description, Module module) {
        super(name, description, module != null ? module.getKeybind() : 0);
        this.boundModule = module;
    }

    // 通用构造
    public KeybindSetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
        this.boundModule = null;
    }

    // 绑定模块
    public void bindToModule(Module module) {
        this.boundModule = module;
        if (module != null) {
            // 设置值但不触发循环
            super.setValue(module.getKeybind());
        }
    }

    public Module getBoundModule() {
        return boundModule;
    }

    // ========== 新增：监听相关方法 ==========

    /**
     * 设置监听状态
     */
    public void setListening(boolean listening) {
        this.isListening = listening;
    }

    /**
     * 获取监听状态
     */
    public boolean isListening() {
        return this.isListening;
    }

    /**
     * 开始监听按键
     */
    public void startListening() {
        this.isListening = true;
    }

    /**
     * 停止监听按键
     */
    public void stopListening() {
        this.isListening = false;
    }

    /**
     * 切换监听状态
     */
    public void toggleListening() {
        this.isListening = !this.isListening;
    }

    /**
     * 处理按键输入（当处于监听状态时）
     * @param keyCode 按下的键码
     * @return 是否处理了这个按键（true=已处理，false=未处理）
     */
    public boolean handleKeyInput(int keyCode) {
        if (!isListening) {
            return false;
        }

        // ESC键取消监听
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            stopListening();
            return true;
        }

        // DELETE键清除绑定
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            setValue(0);
            stopListening();
            return true;
        }

        // 设置新的键值
        setValue(keyCode);
        stopListening();
        return true;
    }

    // ========== 原有方法 ==========

    // 检查按键并触发（带防重复）
    public boolean checkAndTrigger() {
        if (boundModule == null || getValue() == null || getValue() == 0) {
            return false;
        }

        long window = GLFW.glfwGetCurrentContext();
        if (window == 0) return false;

        boolean pressed = GLFW.glfwGetKey(window, getValue()) == GLFW.GLFW_PRESS;

        if (pressed && !isKeyDown) {
            isKeyDown = true;
            boundModule.toggle();
            return true;
        } else if (!pressed) {
            isKeyDown = false;
        }
        return false;
    }

    // 简单检查按键
    public boolean isPressed() {
        Integer key = getValue();
        if (key == null || key == 0) return false;
        long window = GLFW.glfwGetCurrentContext();
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    // 重写setValue以同步模块
    @Override
    public void setValue(Integer value) {
        // 保存旧值用于比较
        Integer oldValue = getValue();
        super.setValue(value);

        // 如果值没有改变，直接返回
        if (oldValue != null && oldValue.equals(value)) {
            return;
        }

        // 如果绑定了模块，更新模块的keybind字段
        if (boundModule != null) {
            // 直接设置模块的keybind字段，不调用setKeybind方法
            boundModule.setKeybindDirect(value != null ? value : 0);

            // 更新KeybindManager
            KeybindManager.getInstance().registerKeybind(boundModule, value != null ? value : 0);
        }

        // 如果正在监听，自动停止监听
        if (isListening && value != null && value != 0) {
            stopListening();
        }
    }

    /** 直接设置值，避免循环（内部使用） */
    public void setValueDirect(Integer value) {
        // 直接调用父类的setValue，不触发模块更新
        super.setValue(value);
    }

    public String getKeyName() {
        Integer key = getValue();
        if (key == null || key == 0) return "未绑定";

        // 使用KeybindManager的静态方法
        return KeybindManager.getKeyName(key);
    }

    // ========== 新增：获取显示文本 ==========

    /**
     * 获取显示文本（用于GUI显示）
     */
    public String getDisplayText() {
        if (isListening) {
            return "按下按键...";
        }
        return getKeyName();
    }
}