package com.example.tianyiclient.modules.render;

import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.BoolSetting;
import com.example.tianyiclient.settings.ColorSetting;
import com.example.tianyiclient.settings.DoubleSetting;
import com.example.tianyiclient.settings.EnumSetting;

public class HUD extends Module {
    // 各种HUD组件的可见性设置
    private final BoolSetting showCoords = new BoolSetting("显示坐标", "显示玩家坐标信息", true);
    private final BoolSetting showFPS = new BoolSetting("显示FPS", "显示帧率信息", true);
    private final BoolSetting showModuleList = new BoolSetting("显示模块列表", "显示已启用的模块列表", true);
    private final BoolSetting showWatermark = new BoolSetting("显示水印", "显示客户端水印", true);
    private final ColorSetting textColor = new ColorSetting("文字颜色", "HUD文字颜色", 0xFFFFFFFF);
    private final DoubleSetting scale = new DoubleSetting("HUD缩放", "HUD整体缩放比例", 1.0, 0.5, 2.0);

    // HUD位置和样式设置 - 使用数组形式避免歧义
    private final EnumSetting hudPosition = new EnumSetting(
            "HUD位置",
            "HUD整体位置预设",
            "左上角",
            new String[]{"左上角", "右上角", "左下角", "右下角", "居中"}
    );

    private final BoolSetting showBackground = new BoolSetting("显示背景", "显示HUD背景框", true);
    private final ColorSetting backgroundColor = new ColorSetting("背景颜色", "HUD背景颜色", 0x80000000);

    public HUD() {
        super("HUD", "显示和配置游戏界面信息", Category.渲染);

        // 添加所有设置项
        addSetting(showCoords);
        addSetting(showFPS);
        addSetting(showModuleList);
        addSetting(showWatermark);
        addSetting(textColor);
        addSetting(scale);
        addSetting(hudPosition);
        addSetting(showBackground);
        addSetting(backgroundColor);
    }

    @Override
    protected void onEnable() {
        System.out.println("[HUD] 游戏界面显示已启用");
    }

    @Override
    protected void onDisable() {
        System.out.println("[HUD] 游戏界面显示已禁用");
    }

    // 供HudRenderer查询设置的方法
    public boolean shouldShowCoords() {
        return isEnabled() && showCoords.getValue();
    }

    public boolean shouldShowFPS() {
        return isEnabled() && showFPS.getValue();
    }

    public boolean shouldShowModuleList() {
        return isEnabled() && showModuleList.getValue();
    }

    public boolean shouldShowWatermark() {
        return isEnabled() && showWatermark.getValue();
    }

    public int getTextColor() {
        return textColor.getValue();
    }

    public double getScale() {
        return scale.getValue();
    }

    public String getHudPosition() {
        return hudPosition.getValue();
    }

    public boolean shouldShowBackground() {
        return showBackground.getValue();
    }

    public int getBackgroundColor() {
        return backgroundColor.getValue();
    }
}