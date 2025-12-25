package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.hud.binding.DataBinding;
import com.example.tianyiclient.hud.binding.DataProviderRegistry;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class TextHudElement extends HudElement {
    private String rawText;        // 原始文本（可能包含 ${变量}）
    private String displayText;    // 渲染时的文本（变量已替换）
    private int color = 0xFFFFFF;
    private boolean shadow = true;
    private int fontSize = 9;

    // 数据绑定列表
    private final List<DataBinding> dataBindings = new ArrayList<>();

    // 支持的变量列表（用于GUI显示）
    private static final String[] SUPPORTED_VARIABLES = {
            "${fps}", "${coords}", "${direction}", "${health}",
            "${hunger}", "${armor}", "${ping}", "${biome}",
            "${light}", "${time}", "${memory}", "${username}",
            "${server}", "${x}", "${y}", "${z}"
    };

    public TextHudElement(String name, float x, float y) {
        super(name, x, y);

        // === 添加样式设置 ===
        getStyleGroup().add(new StringSetting("文本内容", name + " - 文本内容", "TianyiClient ${fps} FPS"));
        getStyleGroup().add(new ColorSetting("颜色", name + " - 颜色", 0xFFFFFFFF));
        getStyleGroup().add(new BoolSetting("阴影", name + " - 阴影", true));
        getStyleGroup().add(new IntegerSetting("字体大小", name + " - 字体大小", 9, 6, 24));

        // === 添加数据设置 ===
        getStyleGroup().add(new BoolSetting("自动绑定变量", name + " - 自动绑定变量", true));

        // 初始化文本
        this.rawText = getSettingValue(getStyleGroup(), "文本内容", String.class, "TianyiClient ${fps} FPS");

        Integer colorValue = getSettingValue(getStyleGroup(), "颜色", Integer.class, 0xFFFFFFFF);
        if (colorValue != null) this.color = colorValue;

        Boolean shadowValue = getSettingValue(getStyleGroup(), "阴影", Boolean.class, true);
        if (shadowValue != null) this.shadow = shadowValue;

        Integer fontSizeValue = getSettingValue(getStyleGroup(), "字体大小", Integer.class, 9);
        if (fontSizeValue != null) this.fontSize = fontSizeValue;

        // 自动检测并绑定变量
        autoBindVariables();

        // 初始计算大小
        updateSize();
    }

    /**
     * 自动检测文本中的变量并绑定对应的数据提供器
     */
    private void autoBindVariables() {
        Boolean autoBind = getSettingValue(getStyleGroup(), "自动绑定变量", Boolean.class, true);
        if (!autoBind || rawText == null) return;

        dataBindings.clear();

        // 检查文本中是否包含支持的变量
        for (String variable : SUPPORTED_VARIABLES) {
            if (rawText.contains(variable)) {
                bindVariable(variable);
            }
        }
    }

    /**
     * 绑定特定变量
     */
    private void bindVariable(String placeholder) {
        DataBinding binding = null;

        switch (placeholder) {
            case "${fps}":
                binding = DataProviderRegistry.getFpsBinding();
                break;
            case "${coords}":
                binding = DataProviderRegistry.getCoordsBinding();
                break;
            case "${x}":
                binding = new DataBinding("${x}", () -> {
                    var player = mc.player;
                    return player != null ? String.format("%.1f", player.getX()) : "0";
                });
                break;
            case "${y}":
                binding = new DataBinding("${y}", () -> {
                    var player = mc.player;
                    return player != null ? String.format("%.1f", player.getY()) : "0";
                });
                break;
            case "${z}":
                binding = new DataBinding("${z}", () -> {
                    var player = mc.player;
                    return player != null ? String.format("%.1f", player.getZ()) : "0";
                });
                break;
            case "${health}":
                binding = DataProviderRegistry.getHealthBinding();
                break;
            case "${ping}":
                binding = DataProviderRegistry.getPingBinding();
                break;
            case "${direction}":
                binding = new DataBinding("${direction}", DataProviderRegistry.DIRECTION::get);
                break;
            case "${hunger}":
                binding = new DataBinding("${hunger}", DataProviderRegistry.HUNGER::get);
                break;
            case "${armor}":
                binding = new DataBinding("${armor}", DataProviderRegistry.ARMOR::get);
                break;
            case "${biome}":
                binding = new DataBinding("${biome}", DataProviderRegistry.BIOME::get);
                break;
            case "${light}":
                binding = new DataBinding("${light}", DataProviderRegistry.LIGHT_LEVEL::get);
                break;
            case "${time}":
                binding = new DataBinding("${time}", DataProviderRegistry.GAME_TIME::get);
                break;
            case "${memory}":
                binding = new DataBinding("${memory}", DataProviderRegistry.MEMORY::get);
                break;
            case "${username}":
                binding = new DataBinding("${username}", DataProviderRegistry.USERNAME::get);
                break;
            case "${server}":
                binding = new DataBinding("${server}", DataProviderRegistry.SERVER_IP::get);
                break;
        }

        if (binding != null) {
            dataBindings.add(binding);
        }
    }

    /**
     * 更新显示文本（替换变量）
     */
    private void updateDisplayText() {
        if (rawText == null) {
            displayText = "";
            return;
        }

        displayText = rawText;
        for (DataBinding binding : dataBindings) {
            if (displayText.contains(binding.getPlaceholder())) {
                displayText = displayText.replace(binding.getPlaceholder(), binding.getValue());
            }
        }
    }

    /**
     * 更新元素大小
     */
    private void updateSize() {
        if (mc.textRenderer != null && displayText != null) {
            setSize(mc.textRenderer.getWidth(displayText) + 4, mc.textRenderer.fontHeight + 4);
        } else {
            setSize(50, 10);
        }
    }

    // ========== 公开方法 ==========

    public void setText(String text) {
        this.rawText = text;
        updateSettingValue(getStyleGroup(), "文本内容", text);
        autoBindVariables(); // 重新绑定变量
    }

    public void setColor(int color) {
        this.color = color;
        updateSettingValue(getStyleGroup(), "颜色", color);
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
        updateSettingValue(getStyleGroup(), "阴影", shadow);
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        updateSettingValue(getStyleGroup(), "字体大小", fontSize);
    }

    public String getText() { return rawText; }
    public int getColor() { return color; }
    public boolean hasShadow() { return shadow; }
    public int getFontSize() { return fontSize; }

    /**
     * 手动添加数据绑定
     */
    public void addDataBinding(DataBinding binding) {
        dataBindings.add(binding);
    }

    /**
     * 获取支持的所有变量
     */
    public static String[] getSupportedVariables() {
        return SUPPORTED_VARIABLES.clone();
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        // 1. 从设置更新基本属性
        updateFromSettings();

        // 2. 获取最新设置值
        String newText = getSettingValue(getStyleGroup(), "文本内容", String.class, rawText);
        if (newText != null && !newText.equals(rawText)) {
            rawText = newText;
            autoBindVariables(); // 文本变化时重新绑定
        }

        Integer newColor = getSettingValue(getStyleGroup(), "颜色", Integer.class, color);
        if (newColor != null) this.color = newColor;

        Boolean newShadow = getSettingValue(getStyleGroup(), "阴影", Boolean.class, shadow);
        if (newShadow != null) this.shadow = newShadow;

        // 3. 更新显示文本（动态数据）
        updateDisplayText();

        if (displayText == null || displayText.isEmpty() || !isVisible()) return;

        // 4. 更新大小
        updateSize();

        // 5. 计算渲染位置和大小（考虑缩放）
        float renderX = getX();
        float renderY = getY();
        float renderWidth = getWidth() * getScale();
        float renderHeight = getHeight() * getScale();

        // 6. 渲染背景（应用透明度）
        int bgAlpha = (int)(0x80 * getAlpha());
        int bgColor = (bgAlpha << 24) | 0x000000;
        context.fill((int) renderX, (int) renderY,
                (int) (renderX + renderWidth), (int) (renderY + renderHeight),
                bgColor);

        // 7. 渲染文本（应用颜色和透明度）
        int textAlpha = (int)(0xFF * getAlpha());
        int textColor = (textAlpha << 24) | (this.color & 0x00FFFFFF);

        context.drawText(mc.textRenderer, Text.literal(displayText),
                (int) renderX + 2, (int) renderY + 2,
                textColor, shadow);
    }
}