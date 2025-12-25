package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 显示启用的模块列表
 */
public class ModuleListElement extends HudElement {

    public ModuleListElement() {
        super("模块列表", 10.0f, 100.0f);

        // 添加设置
        getStyleGroup().add(new ColorSetting("文字颜色", "模块列表 - 文字颜色", 0xFFFFFFFF));
        getStyleGroup().add(new ColorSetting("背景颜色", "模块列表 - 背景颜色", 0x80000000));
        getStyleGroup().add(new BoolSetting("显示标题", "模块列表 - 显示标题", true));
        getStyleGroup().add(new BoolSetting("紧凑模式", "模块列表 - 紧凑模式", false));
        getStyleGroup().add(new BoolSetting("阴影", "模块列表 - 文字阴影", true));
        getStyleGroup().add(new IntegerSetting("字体大小", "模块列表 - 字体大小", 9, 6, 24));

        // 设置初始大小
        setSize(100, 50);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isVisible()) return;

        updateFromSettings();

        // 获取设置值
        int textColor = getSettingValue(getStyleGroup(), "文字颜色", Integer.class, 0xFFFFFFFF);
        int bgColor = getSettingValue(getStyleGroup(), "背景颜色", Integer.class, 0x80000000);
        Boolean showTitle = getSettingValue(getStyleGroup(), "显示标题", Boolean.class, true);
        Boolean compactMode = getSettingValue(getStyleGroup(), "紧凑模式", Boolean.class, false);
        Boolean shadow = getSettingValue(getStyleGroup(), "阴影", Boolean.class, true);

        // 获取启用的模块
        List<Module> enabledModules = TianyiClient.getInstance().getModuleManager()
                .getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparing(Module::getName))
                .collect(Collectors.toList());

        if (enabledModules.isEmpty()) return;

        if (compactMode) {
            renderCompactMode(context, enabledModules, textColor, bgColor, shadow);
        } else {
            renderDetailedMode(context, enabledModules, textColor, bgColor, showTitle, shadow);
        }
    }

    private void renderCompactMode(DrawContext context, List<Module> modules, int textColor, int bgColor, boolean shadow) {
        int startX = (int) getX();
        int y = (int) getY();
        int currentX = startX;
        int maxWidth = 0;

        // 计算总宽度和位置
        for (Module module : modules) {
            String name = module.getName();
            int width = mc.textRenderer.getWidth(name) + 8;
            maxWidth += width + 2;
        }

        // 设置位置（如果太靠右，向左移动）
        if (startX + maxWidth > mc.getWindow().getScaledWidth()) {
            startX = mc.getWindow().getScaledWidth() - maxWidth - 10;
            setPosition(startX, getY());
        }

        // 渲染模块
        currentX = startX;
        for (Module module : modules) {
            String name = module.getName();
            int width = mc.textRenderer.getWidth(name) + 8;
            int height = 12;

            // 背景
            context.fill(currentX, y, currentX + width, y + height, bgColor);

            // 左侧指示器
            context.fill(currentX, y, currentX + 2, y + height, 0xFF00FF00);

            // 名称
            context.drawText(mc.textRenderer, Text.literal(name),
                    currentX + 4, y + 2, textColor, shadow);

            currentX += width + 2;
        }

        // 更新大小
        setSize(maxWidth, 12);
    }

    private void renderDetailedMode(DrawContext context, List<Module> modules, int textColor, int bgColor, boolean showTitle, boolean shadow) {
        int x = (int) getX();
        int y = (int) getY();

        // 计算最大宽度
        int maxWidth = 0;
        for (Module module : modules) {
            int width = mc.textRenderer.getWidth("› " + module.getName());
            if (width > maxWidth) maxWidth = width;
        }

        // 标题宽度
        if (showTitle) {
            int titleWidth = mc.textRenderer.getWidth("📦 启用模块");
            if (titleWidth > maxWidth) maxWidth = titleWidth;
        }

        // 计算高度
        int itemHeight = 16;
        int spacing = 2;
        int titleHeight = showTitle ? 20 : 0;
        int listHeight = modules.size() * (itemHeight + spacing);
        int totalHeight = titleHeight + listHeight + 8;

        // 设置位置（如果太靠右，向左移动）
        if (x + maxWidth + 24 > mc.getWindow().getScaledWidth()) {
            x = mc.getWindow().getScaledWidth() - maxWidth - 24 - 10;
            setPosition(x, y);
        }

        // 背景
        context.fill(x - 4, y, x + maxWidth + 20, y + totalHeight, bgColor);

        int currentY = y + 4;

        // 标题
        if (showTitle) {
            context.drawText(mc.textRenderer, Text.literal("📦 启用模块"),
                    x, currentY, textColor, shadow);
            currentY += 16;
        }

        // 模块列表
        for (Module module : modules) {
            // 背景
            context.fill(x, currentY, x + maxWidth + 16, currentY + itemHeight, 0x20FFFFFF);

            // 左侧指示器
            context.fill(x, currentY, x + 2, currentY + itemHeight, 0xFF00FF00);

            // 名称
            context.drawText(mc.textRenderer, Text.literal("› " + module.getName()),
                    x + 6, currentY + (itemHeight - 9) / 2, textColor, shadow);

            currentY += itemHeight + spacing;
        }

        // 更新大小
        setSize(maxWidth + 24, totalHeight);
    }
}