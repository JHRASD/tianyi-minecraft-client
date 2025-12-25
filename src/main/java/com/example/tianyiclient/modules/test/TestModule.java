package com.example.tianyiclient.modules.test;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.client.KeyEvent;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import org.lwjgl.glfw.GLFW;

public class TestModule extends Module {

    private int tickCounter = 0;

    public TestModule() {
        super("测试模块", "用于测试事件系统", Category.玩家);
        // 设置默认快捷键为F6
        setKeybind(GLFW.GLFW_KEY_F6);
    }

    @Override
    protected void onEnable() {
        System.out.println("[TestModule] 测试模块已启用");
    }

    @Override
    protected void onDisable() {
        System.out.println("[TestModule] 测试模块已禁用");
    }

    /**
     * 监听按键事件
     */
    @EventHandler(priority = Priority.NORMAL)
    public void onKeyEvent(KeyEvent event) {
        // 检查是否是F6按键按下事件
        if (event.getKeyCode() == GLFW.GLFW_KEY_F6 && event.isPress()) {
            System.out.println("[TestModule] F6按键被按下，切换模块状态");
            toggle();

            // 你也可以使用 KeybindManager 的逻辑，这里简单处理
            System.out.println("[TestModule] 模块状态: " + (isEnabled() ? "已启用" : "已禁用"));
        }
    }

    /**
     * 监听Tick事件
     */
    @EventHandler(priority = Priority.NORMAL)
    public void onTickEvent(TickEvent event) {
        if (isEnabled()) {
            tickCounter++;
            if (tickCounter % 20 == 0) { // 每20tick（1秒）输出一次
                System.out.println("[TestModule] Tick事件: 阶段=" + event.getPhase() +
                        ", 模块已启用 " + (tickCounter/20) + " 秒");
            }
        }
    }
}