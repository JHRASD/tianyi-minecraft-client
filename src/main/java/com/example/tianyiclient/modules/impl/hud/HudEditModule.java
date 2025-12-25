package com.example.tianyiclient.modules.impl.hud;

import com.example.tianyiclient.gui.hud.HudEditorScreen; // 新增导入
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.hud.HudManager;
import com.example.tianyiclient.settings.KeybindSetting;

public class HudEditModule extends Module {
    private final HudManager hudManager;
    private KeybindSetting keybindSetting;

    public HudEditModule(HudManager hudManager) {
        super("HUD编辑模式", "编辑HUD元素的位置和内容", Category.渲染);
        this.hudManager = hudManager;

        // 初始化模块设置
        initModuleSettings();
    }

    private void initModuleSettings() {
        // 创建按键绑定设置
        keybindSetting = new KeybindSetting("快捷键", "HUD编辑模式快捷键", this);
        addSetting(keybindSetting);

        // 设置默认快捷键为 H 键
        keybindSetting.setValueDirect(72); // H 键的键码是 72
    }

    @Override
    protected void onEnable() {
        System.out.println("[HUD编辑] 进入编辑模式");
        if (mc.currentScreen == null) { // 不在其他GUI中
            // 直接打开编辑器屏幕
            mc.setScreen(new HudEditorScreen());
        }
    }

    @Override
    protected void onDisable() {
        System.out.println("[HUD编辑] 退出编辑模式");
        // 关闭编辑器屏幕
        if (mc.currentScreen instanceof HudEditorScreen) {
            mc.setScreen(null);
        }
    }

    /**
     * 获取按键绑定设置
     */
    public KeybindSetting getKeybindSetting() {
        return keybindSetting;
    }

    /**
     * 处理按键按下事件
     */
    public boolean onKeyPressed(int keyCode) {
        if (keybindSetting != null && keybindSetting.getValue() == keyCode) {
            toggle();
            return true;
        }
        return false;
    }
}