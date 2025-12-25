package com.example.tianyiclient.gui;

import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGuiWindow {
    // 窗口属性
    private int x, y;
    private int width = 120;  // 减小宽度让分类窗口更紧凑
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;

    // UI 状态
    private Module selectedModule = null;
    private final Category category;
    private final List<Module> modules;
    private final Map<Module, Float> hoverAnimations = new HashMap<>();
    private final Map<Module, Float> enabledAnimations = new HashMap<>();

    // 滑块拖动状态
    private boolean isSliderDragging = false;
    private DoubleSetting currentDraggingSlider = null;
    private int sliderDragStartX = 0;
    private double sliderDragStartValue = 0;

    // 文字滑块拖动状态（新增）
    private boolean isTextSliderDragging = false;
    private DoubleSetting currentDraggingTextSlider = null;
    private int textSliderDragStartX = 0;
    private double textSliderDragStartValue = 0;

    // 样式常量 - 进一步减小高度
    private static final int HEADER_HEIGHT = 20;  // 更小的标题栏
    private static final int MODULE_HEIGHT = 18;  // 更小的模块高度
    private static final int SETTING_HEIGHT = 16;  // 更小的设置高度
    private static final int PADDING = 4;
    private static final int SMALL_PADDING = 2;
    private static final int CORNER_RADIUS = 3;
    private static final int SWITCH_WIDTH = 28;
    private static final int SLIDER_HEIGHT = 2;
    private static final int SLIDER_HANDLE_SIZE = 6;

    // 静态变量用于快捷键监听
    private static KeybindSetting listeningKeybind = null;

    public ClickGuiWindow(int x, int y, Category category) {
        this.x = x;
        this.y = y;
        this.category = category;
        this.modules = getModulesByCategory(category);

        // 初始化动画
        for (Module module : modules) {
            hoverAnimations.put(module, 0f);
            enabledAnimations.put(module, module.isEnabled() ? 1f : 0f);
        }
    }

    // ========== 渲染方法 ==========
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 更新动画
        updateAnimations(delta, mouseX, mouseY);

        // 计算窗口高度
        int windowHeight = calculateWindowHeight();

        // 渲染窗口背景
        context.fill(x, y, x + width, y + windowHeight, 0xFF1A1A1A);

        // 渲染边框
        context.fill(x, y, x + width, y + 1, 0xFF252525);
        context.fill(x, y + windowHeight - 1, x + width, y + windowHeight, 0xFF252525);
        context.fill(x, y, x + 1, y + windowHeight, 0xFF252525);
        context.fill(x + width - 1, y, x + width, y + windowHeight, 0xFF252525);

        // 渲染分割线
        context.fill(x, y + HEADER_HEIGHT, x + width, y + HEADER_HEIGHT + 1, 0xFF252525);

        // 渲染内容
        if (selectedModule == null) {
            renderMainInterface(context, mouseX, mouseY, windowHeight);
        } else {
            renderSettingsInterface(context, mouseX, mouseY, windowHeight);
        }
    }

    private int calculateWindowHeight() {
        if (selectedModule == null) {
            return HEADER_HEIGHT + modules.size() * MODULE_HEIGHT + 2;
        } else {
            List<Setting<?>> settings = selectedModule.getSettings();
            int settingsHeight = (settings != null ? settings.size() * SETTING_HEIGHT : 0);
            return HEADER_HEIGHT + settingsHeight + 2;
        }
    }

    private void updateAnimations(float delta, int mouseX, int mouseY) {
        for (Module module : modules) {
            boolean hovered = isModuleHovered(mouseX, mouseY, module);
            float targetHover = hovered ? 1f : 0f;
            hoverAnimations.put(module,
                    MathHelper.lerp(0.2f, hoverAnimations.getOrDefault(module, 0f), targetHover));

            float targetEnabled = module.isEnabled() ? 1f : 0f;
            enabledAnimations.put(module,
                    MathHelper.lerp(0.1f, enabledAnimations.getOrDefault(module, 0f), targetEnabled));
        }
    }

    private boolean isModuleHovered(int mouseX, int mouseY, Module module) {
        if (selectedModule != null) return false;

        int moduleY = y + HEADER_HEIGHT + 1;
        int index = modules.indexOf(module);

        if (index != -1) {
            moduleY += index * MODULE_HEIGHT;
            return isMouseOver(mouseX, mouseY, x + 1, moduleY, width - 2, MODULE_HEIGHT - 1);
        }
        return false;
    }

    private void renderMainInterface(DrawContext context, int mouseX, int mouseY, int windowHeight) {
        // 标题栏
        renderHeader(context, category.getDisplayName(), false);

        // 模块列表
        int moduleY = y + HEADER_HEIGHT + 1;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            renderModuleItem(context, module, x + 1, moduleY, width - 2, MODULE_HEIGHT - 1, mouseX, mouseY);
            moduleY += MODULE_HEIGHT;
        }
    }

    private void renderSettingsInterface(DrawContext context, int mouseX, int mouseY, int windowHeight) {
        if (selectedModule == null) return;

        // 标题栏（带返回按钮）
        renderHeader(context, selectedModule.getName(), true);

        // 设置列表
        List<Setting<?>> settings = selectedModule.getSettings();
        if (settings == null || settings.isEmpty()) {
            String text = "无设置";
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (windowHeight + HEADER_HEIGHT) / 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    text, textX, textY, 0xFF808080);
            return;
        }

        // 渲染设置项
        int settingY = y + HEADER_HEIGHT + 1;
        for (Setting<?> setting : settings) {
            renderSettingItem(context, setting, x + 1, settingY, width - 2, SETTING_HEIGHT, mouseX, mouseY);
            settingY += SETTING_HEIGHT;
        }
    }

    private void renderHeader(DrawContext context, String title, boolean showBack) {
        // 标题栏背景
        context.fill(x, y, x + width, y + HEADER_HEIGHT, 0xFF252525);

        int titleY = y + (HEADER_HEIGHT - 8) / 2;

        if (showBack) {
            // 返回按钮和标题在同一行
            String backText = "< " + title;
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(backText);

            // 如果太长则截断
            if (textWidth > width - 10) {
                backText = MinecraftClient.getInstance().textRenderer.trimToWidth(backText, width - 10) + "...";
                textWidth = MinecraftClient.getInstance().textRenderer.getWidth(backText);
            }

            int textX = x + (width - textWidth) / 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    backText, textX, titleY, 0xFFF5F5F5);
        } else {
            // 居中标题
            int titleWidth = MinecraftClient.getInstance().textRenderer.getWidth(title);
            int titleX = x + (width - titleWidth) / 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    title, titleX, titleY, 0xFFF5F5F5);
        }
    }

    private void renderModuleItem(DrawContext context, Module module, int x, int y, int width, int height, int mouseX, int mouseY) {
        float hoverAlpha = hoverAnimations.getOrDefault(module, 0f);
        float enabledAlpha = enabledAnimations.getOrDefault(module, 0f);
        boolean hovered = isMouseOver(mouseX, mouseY, x, y, width, height);

        // 背景颜色
        int bgColor = 0xFF212121;
        if (hovered) {
            bgColor = 0xFF2A2A2A;
        }
        if (module.isEnabled()) {
            bgColor = blendColors(bgColor, 0xFF4CAF50, enabledAlpha * 0.2f);
        }

        // 模块背景
        context.fill(x, y, x + width, y + height, bgColor);

        // 模块名称
        String moduleName = module.getName();
        int nameX = x + 4;
        int nameY = y + (height - 8) / 2;

        // 截断过长的模块名
        int maxNameWidth = width - 24;
        int nameWidth = MinecraftClient.getInstance().textRenderer.getWidth(moduleName);
        if (nameWidth > maxNameWidth) {
            moduleName = MinecraftClient.getInstance().textRenderer.trimToWidth(moduleName, maxNameWidth) + "..";
        }

        int textColor = module.isEnabled() ? 0xFFF5F5F5 : 0xFFB0B0B0;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                moduleName, nameX, nameY, textColor);

        // 设置图标（如果有设置）
        if (module.getSettings() != null && !module.getSettings().isEmpty()) {
            String gear = "⚙";
            int gearWidth = MinecraftClient.getInstance().textRenderer.getWidth(gear);
            int gearX = x + width - gearWidth - 4;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    gear, gearX, nameY, 0xFF808080);
        }
    }

    // ========== 【设置界面区域 - START】 ==========
    // 【卡片式设计版本 - 用卡片背景色表示进度和状态】

    // 卡片配色方案
    private static final int CARD_BG_NORMAL = 0x80212121;     // 正常卡片背景
    private static final int CARD_BG_HOVER = 0x80353535;      // 悬停卡片背景
    private static final int CARD_BG_ACTIVE = 0x80304CAF;     // 激活状态（开启/选中）蓝色
    private static final int CARD_BG_SLIDER_BASE = 0x802D2D2D; // 滑块卡片底色
    private static final int CARD_BG_SLIDER_FILL = 0x804A90E2; // 滑块填充色

    // 文字颜色
    private static final int TEXT_NORMAL = 0xFFF0F0F0;        // 正常白色
    private static final int TEXT_ACTIVE = 0xFFFFFFFF;        // 激活白色（高亮）
    private static final int TEXT_INACTIVE = 0xFFAAAAAA;      // 非激活灰色
    private static final int TEXT_HIGHLIGHT = 0xFFFFFF00;     // 高亮黄色

    private void renderSettingItem(DrawContext context, Setting<?> setting, int x, int y, int width, int height,
                                   int mouseX, int mouseY) {
        boolean hovered = isMouseOver(mouseX, mouseY, x, y, width, height);

        // 根据设置类型渲染不同的卡片效果
        if (setting instanceof BoolSetting boolSetting) {
            renderCardBoolSetting(context, boolSetting, x, y, width, height, hovered);
        } else if (setting instanceof DoubleSetting doubleSetting) {
            renderCardSliderSetting(context, doubleSetting, x, y, width, height, mouseX, mouseY, hovered);
        } else if (setting instanceof KeybindSetting keybindSetting) {
            renderCardKeybindSetting(context, keybindSetting, x, y, width, height, hovered);
        } else if (setting instanceof EnumSetting enumSetting) {
            renderCardEnumSetting(context, enumSetting, x, y, width, height, hovered);
        } else {
            // 其他设置类型使用默认卡片
            renderDefaultCard(context, setting, x, y, width, height, hovered);
        }
    }

    /**
     * 【卡片式布尔设置】- 整个卡片变色表示开关状态
     */
    private void renderCardBoolSetting(DrawContext context, BoolSetting setting, int x, int y,
                                       int width, int height, boolean hovered) {
        boolean isOn = setting.getValue();

        // 卡片背景色：开启时为激活蓝色，关闭时为正常/悬停色
        int cardBgColor;
        if (isOn) {
            cardBgColor = CARD_BG_ACTIVE; // 开启状态 - 蓝色卡片
        } else {
            cardBgColor = hovered ? CARD_BG_HOVER : CARD_BG_NORMAL;
        }

        // 渲染卡片背景（圆角效果）
        renderCardBackground(context, x, y, width, height, cardBgColor, isOn);

        // 卡片内文字
        String text = setting.getName();
        int textY = y + (height - 8) / 2;
        int textX = x + 8; // 左边距

        // 文字颜色：根据状态变化
        int textColor = isOn ? TEXT_ACTIVE : (hovered ? TEXT_ACTIVE : TEXT_NORMAL);

        // 文本过长处理
        if (MinecraftClient.getInstance().textRenderer.getWidth(text) > width - 16) {
            text = MinecraftClient.getInstance().textRenderer.trimToWidth(text, width - 16) + "..";
        }

        // 渲染文字
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, textX, textY, textColor);

        // 状态指示器（右侧小圆点）
        if (isOn) {
            int dotX = x + width - 12;
            int dotY = y + height / 2 - 2;
            context.fill(dotX, dotY, dotX + 4, dotY + 4, 0xFFFFFF00); // 黄色圆点
        }
    }

    /**
     * 【卡片式滑块设置】- 卡片背景色表示进度
     */
    private void renderCardSliderSetting(DrawContext context, DoubleSetting setting, int x, int y,
                                         int width, int height, int mouseX, int mouseY, boolean hovered) {
        double value = setting.getValue();
        double min = setting.getMin();
        double max = setting.getMax();
        float progress = (float) ((value - min) / (max - min));

        // 判断是否正在拖拽
        boolean isDraggingThis = isTextSliderDragging && currentDraggingTextSlider == setting;

        // 【关键：卡片背景色表示进度】
        // 卡片分为两部分：填充色（进度）和底色（剩余）
        int fillWidth = (int) ((width - 4) * progress); // 留出边距
        int fillX = x + 2;

        // 渲染卡片底色（整个卡片）
        context.fill(x, y, x + width, y + height, CARD_BG_SLIDER_BASE);

        // 渲染进度填充色（覆盖部分卡片）
        int fillColor = isDraggingThis ? 0x8064B5F6 : CARD_BG_SLIDER_FILL;
        context.fill(fillX, y + 1, fillX + fillWidth, y + height - 1, fillColor);

        // 卡片边框效果
        if (hovered || isDraggingThis) {
            // 高亮边框
            context.fill(x, y, x + width, y + 1, 0x80FFFFFF); // 顶部边框
            context.fill(x, y + height - 1, x + width, y + height, 0x80FFFFFF); // 底部边框
            context.fill(x, y, x + 1, y + height, 0x80FFFFFF); // 左边框
            context.fill(x + width - 1, y, x + width, y + height, 0x80FFFFFF); // 右边框
        }

        // 卡片内文字和数值
        String name = setting.getName();
        String valueStr = String.format("%.1f", value);

        int textY = y + (height - 8) / 2;

        // 渲染名称（左侧）
        int nameX = x + 8;
        String nameText = name;
        int maxNameWidth = width / 2 - 16;

        if (MinecraftClient.getInstance().textRenderer.getWidth(nameText) > maxNameWidth) {
            nameText = MinecraftClient.getInstance().textRenderer.trimToWidth(name, maxNameWidth) + "..";
        }

        int nameColor = isDraggingThis ? TEXT_HIGHLIGHT : TEXT_ACTIVE;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, nameText, nameX, textY, nameColor);

        // 渲染数值（右侧）
        int valueX = x + width - 8 - MinecraftClient.getInstance().textRenderer.getWidth(valueStr);
        int valueColor = isDraggingThis ? TEXT_HIGHLIGHT : 0xFF64B5F6;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, valueStr, valueX, textY, valueColor);

        // 拖拽指示器（进度条上的手柄）
        if (isDraggingThis || hovered) {
            int handleX = fillX + fillWidth - 3;
            int handleY = y + height / 2 - 3;

            // 手柄（白色圆点）
            context.fill(handleX, handleY, handleX + 6, handleY + 6, 0xFFFFFFFF);

            // 手柄外圈（发光效果）
            context.fill(handleX - 1, handleY - 1, handleX + 7, handleY + 7, 0x40FFFFFF);
        }

        // 拖拽时显示横向指示箭头
        if (isDraggingThis) {
            int centerX = x + width / 2;
            int arrowY = y - 12;

            // 左箭头
            String leftArrow = "◀";
            int leftWidth = MinecraftClient.getInstance().textRenderer.getWidth(leftArrow);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    leftArrow, centerX - leftWidth - 10, arrowY, TEXT_HIGHLIGHT);

            // 右箭头
            String rightArrow = "▶";
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    rightArrow, centerX + 10, arrowY, TEXT_HIGHLIGHT);

            // 拖拽提示文字
            String dragText = "拖拽调节";
            int dragWidth = MinecraftClient.getInstance().textRenderer.getWidth(dragText);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    dragText, centerX - dragWidth / 2, arrowY, TEXT_HIGHLIGHT);
        }
    }

    /**
     * 【卡片式快捷键设置】
     */
    private void renderCardKeybindSetting(DrawContext context, KeybindSetting setting, int x, int y,
                                          int width, int height, boolean hovered) {
        boolean isListening = setting.isListening();

        // 卡片背景色：监听模式为橙色，否则为正常/悬停色
        int cardBgColor;
        if (isListening) {
            cardBgColor = 0x80FF9800; // 监听模式 - 橙色
        } else {
            cardBgColor = hovered ? CARD_BG_HOVER : CARD_BG_NORMAL;
        }

        // 渲染卡片背景
        renderCardBackground(context, x, y, width, height, cardBgColor, isListening);

        // 卡片内文字
        String text = setting.getName();
        String keyText = isListening ? "按下按键..." : "[" + setting.getKeyName() + "]";

        int textY = y + (height - 8) / 2;

        // 渲染设置名称（左侧）
        int nameX = x + 8;
        String nameText = text;

        if (MinecraftClient.getInstance().textRenderer.getWidth(nameText) > width / 2 - 12) {
            nameText = MinecraftClient.getInstance().textRenderer.trimToWidth(text, width / 2 - 12) + "..";
        }

        int nameColor = isListening ? TEXT_HIGHLIGHT : TEXT_ACTIVE;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, nameText, nameX, textY, nameColor);

        // 渲染按键（右侧）
        int keyX = x + width - 8 - MinecraftClient.getInstance().textRenderer.getWidth(keyText);
        int keyColor = isListening ? TEXT_HIGHLIGHT : 0xFF4CAF50;

        // 按键背景（圆角矩形）
        if (!isListening) {
            int keyBgX = keyX - 4;
            int keyBgY = textY - 2;
            int keyBgWidth = MinecraftClient.getInstance().textRenderer.getWidth(keyText) + 8;
            context.fill(keyBgX, keyBgY, keyBgX + keyBgWidth, keyBgY + 12, 0x404CAF50);
        }

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, keyText, keyX, textY, keyColor);
    }

    /**
     * 【卡片式枚举设置】
     */
    private void renderCardEnumSetting(DrawContext context, EnumSetting setting,
                                       int x, int y, int width, int height, boolean hovered) {
        String name = setting.getName();
        String value = setting.getValue();

        // 卡片背景色
        int cardBgColor = hovered ? CARD_BG_HOVER : CARD_BG_NORMAL;
        renderCardBackground(context, x, y, width, height, cardBgColor, hovered);

        // 卡片内文字
        int textY = y + (height - 8) / 2;

        // 渲染名称（左侧）
        int nameX = x + 8;
        String nameText = name + ":";

        if (MinecraftClient.getInstance().textRenderer.getWidth(nameText) > width / 2 - 12) {
            nameText = MinecraftClient.getInstance().textRenderer.trimToWidth(name, width / 2 - 12) + "..:";
        }

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, nameText, nameX, textY,
                hovered ? TEXT_ACTIVE : TEXT_NORMAL);

        // 渲染值（右侧，带箭头）
        String valueText = value + " ▼";
        int valueX = x + width - 8 - MinecraftClient.getInstance().textRenderer.getWidth(valueText);

        int valueColor = hovered ? 0xFF4CAF50 : 0xFF64B5F6;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, valueText, valueX, textY, valueColor);
    }

    /**
     * 【默认卡片】- 用于其他未知设置类型
     */
    private void renderDefaultCard(DrawContext context, Setting<?> setting, int x, int y,
                                   int width, int height, boolean hovered) {
        // 卡片背景
        int cardBgColor = hovered ? CARD_BG_HOVER : CARD_BG_NORMAL;
        renderCardBackground(context, x, y, width, height, cardBgColor, false);

        // 文字
        String name = setting.getName();
        int textY = y + (height - 8) / 2;
        int textX = x + 8;

        if (MinecraftClient.getInstance().textRenderer.getWidth(name) > width - 16) {
            name = MinecraftClient.getInstance().textRenderer.trimToWidth(name, width - 16) + "..";
        }

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                name, textX, textY, hovered ? TEXT_ACTIVE : TEXT_NORMAL);
    }

    /**
     * 【通用卡片背景渲染】- 创建现代卡片效果
     */
    private void renderCardBackground(DrawContext context, int x, int y, int width, int height,
                                      int bgColor, boolean isActive) {
        // 主卡片背景
        context.fill(x, y, x + width, y + height, bgColor);

        // 卡片边框和阴影效果
        if (isActive) {
            // 激活状态 - 发光边框
            context.fill(x, y, x + width, y + 1, 0x80FFFFFF); // 顶部边框
            context.fill(x, y + height - 1, x + width, y + height, 0x80FFFFFF); // 底部边框
            context.fill(x, y, x + 1, y + height, 0x80FFFFFF); // 左边框
            context.fill(x + width - 1, y, x + width, y + height, 0x80FFFFFF); // 右边框

            // 内发光效果
            context.fill(x + 1, y + 1, x + width - 1, y + 2, 0x40FFFFFF); // 顶部内发光
        } else {
            // 正常状态 - 细微边框
            context.fill(x, y, x + width, y + 1, 0x202020); // 顶部阴影
            context.fill(x, y + height - 1, x + width, y + height, 0x404040); // 底部高光
        }

        // 卡片圆角效果（四角小圆点）
        int cornerRadius = 2;

        // 左上角
        context.fill(x, y, x + cornerRadius, y + cornerRadius, bgColor);
        // 右上角
        context.fill(x + width - cornerRadius, y, x + width, y + cornerRadius, bgColor);
        // 左下角
        context.fill(x, y + height - cornerRadius, x + cornerRadius, y + height, bgColor);
        // 右下角
        context.fill(x + width - cornerRadius, y + height - cornerRadius, x + width, y + height, bgColor);
    }

    /**
     * 【卡片式滑块拖拽逻辑】- 在onDragged方法中调用
     */
    private void handleCardSliderDrag(int mouseX, int mouseY) {
        if (isTextSliderDragging && currentDraggingTextSlider != null) {
            double min = currentDraggingTextSlider.getMin();
            double max = currentDraggingTextSlider.getMax();

            // 根据鼠标在卡片上的水平位置计算进度
            // 需要知道当前拖拽的卡片位置和宽度
            // 这里简化处理，使用相对拖拽距离

            float dragDelta = mouseX - textSliderDragStartX;
            double valueChange = dragDelta * (max - min) / 100.0; // 灵敏度

            double newValue = textSliderDragStartValue + valueChange;
            newValue = MathHelper.clamp(newValue, min, max);
            newValue = Math.round(newValue * 10.0) / 10.0; // 保留1位小数

            currentDraggingTextSlider.setValue(newValue);
        }
    }
    // ========== 【设置界面区域 - END】 ==========

    // ========== 交互方法 ==========
    public boolean onClicked(int mouseX, int mouseY, int button) {
        int windowHeight = calculateWindowHeight();

        // 检查是否点击了窗口区域
        if (!isMouseOver(mouseX, mouseY, x, y, width, windowHeight)) {
            return false;
        }

        // 标题栏拖动
        if (isMouseOver(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (button == 0) {
                isDragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            }
        }

        // 检查是否点击了返回区域（设置界面的标题栏）
        if (selectedModule != null) {
            if (isMouseOver(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
                if (button == 0) {
                    selectedModule = null;
                    return true;
                }
            }
        }

        // 根据当前状态处理点击
        if (selectedModule == null) {
            return handleModuleClick(mouseX, mouseY, button);
        } else {
            return handleSettingClick(mouseX, mouseY, button);
        }
    }

    private boolean handleModuleClick(int mouseX, int mouseY, int button) {
        int moduleY = y + HEADER_HEIGHT + 1;

        for (Module module : modules) {
            if (isMouseOver(mouseX, mouseY, x + 1, moduleY, width - 2, MODULE_HEIGHT - 1)) {
                if (button == 0) { // 左键开关
                    module.toggle();
                    return true;
                } else if (button == 1) { // 右键设置
                    if (module.getSettings() != null && !module.getSettings().isEmpty()) {
                        selectedModule = module;
                        return true;
                    }
                }
            }
            moduleY += MODULE_HEIGHT;
        }

        return false;
    }

    private boolean handleSettingClick(int mouseX, int mouseY, int button) {
        if (selectedModule == null) return false;

        List<Setting<?>> settings = selectedModule.getSettings();
        if (settings == null) return false;

        int settingY = y + HEADER_HEIGHT + 1;

        for (Setting<?> setting : settings) {
            if (isMouseOver(mouseX, mouseY, x + 1, settingY, width - 2, SETTING_HEIGHT)) {
                if (button == 0) { // 左键点击
                    // 【1. 布尔设置 - 点击切换】
                    if (setting instanceof BoolSetting boolSetting) {
                        boolSetting.toggle();
                        return true;
                    }
                    // 【2. 数值设置 - 开始拖拽】
                    else if (setting instanceof DoubleSetting doubleSetting) {
                        // 点击卡片滑块开始拖拽
                        isTextSliderDragging = true;
                        currentDraggingTextSlider = doubleSetting;
                        textSliderDragStartX = mouseX;
                        textSliderDragStartValue = doubleSetting.getValue();
                        return true;
                    }
                    // 【3. 快捷键设置 - 进入监听模式】
                    else if (setting instanceof KeybindSetting keybindSetting) {
                        // 停止之前正在监听的快捷键
                        if (listeningKeybind != null && listeningKeybind != keybindSetting) {
                            listeningKeybind.stopListening();
                        }

                        // 切换当前快捷键的监听状态
                        keybindSetting.toggleListening();

                        // 更新静态变量
                        if (keybindSetting.isListening()) {
                            listeningKeybind = keybindSetting;
                        } else {
                            listeningKeybind = null;
                        }
                        return true;
                    }
                    // 【4. 枚举设置 - 循环切换】
                    else if (setting instanceof EnumSetting enumSetting) {
                        enumSetting.cycle();
                        return true;
                    }
                }
                // 右键点击设置项（可添加其他功能）
                else if (button == 1) {
                    // 这里可以添加右键菜单或其他功能
                    return true;
                }
            }
            settingY += SETTING_HEIGHT;
        }

        // 右键点击设置界面空白处返回主界面
        if (button == 1) {
            selectedModule = null;
            return true;
        }

        return false;
    }

    public void onDragged(int mouseX, int mouseY) {
        if (isDragging) {
            // 窗口拖动
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
            clampToScreen();
        } else if (isTextSliderDragging && currentDraggingTextSlider != null) {
            // 【卡片滑块拖拽逻辑】
            // 计算拖拽距离对应的数值变化
            double min = currentDraggingTextSlider.getMin();
            double max = currentDraggingTextSlider.getMax();

            // 每移动1像素改变 (max-min)/80 的值（可调节灵敏度）
            float dragDelta = mouseX - textSliderDragStartX;
            double valueChange = dragDelta * (max - min) / 80.0;

            double newValue = textSliderDragStartValue + valueChange;
            newValue = MathHelper.clamp(newValue, min, max);

            // 保留1位小数
            newValue = Math.round(newValue * 10.0) / 10.0;

            currentDraggingTextSlider.setValue(newValue);
        }
    }

    public void onMouseReleased() {
        // 释放所有拖拽状态
        isDragging = false;
        isSliderDragging = false;
        currentDraggingSlider = null;
        isTextSliderDragging = false;
        currentDraggingTextSlider = null;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningKeybind != null) {
            // 使用KeybindSetting的handleKeyInput方法
            boolean handled = listeningKeybind.handleKeyInput(keyCode);

            // 如果按键被处理了，更新监听状态
            if (handled) {
                // 检查当前KeybindSetting是否还在监听
                if (!listeningKeybind.isListening()) {
                    listeningKeybind = null;
                }
            }
            return handled;
        }
        return false;
    }

    // ========== 工具方法 ==========
    private void clampToScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int windowHeight = calculateWindowHeight();

            x = MathHelper.clamp(x, 0, screenWidth - width);
            y = MathHelper.clamp(y, 0, screenHeight - windowHeight);
        }
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private List<Module> getModulesByCategory(Category category) {
        try {
            if (com.example.tianyiclient.TianyiClient.INSTANCE != null &&
                    com.example.tianyiclient.TianyiClient.INSTANCE.getModuleManager() != null) {
                return com.example.tianyiclient.TianyiClient.INSTANCE.getModuleManager().getModulesByCategory(category);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private int blendColors(int color1, int color2, float ratio) {
        ratio = MathHelper.clamp(ratio, 0, 1);
        float invRatio = 1 - ratio;

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 * invRatio + a2 * ratio);
        int r = (int) (r1 * invRatio + r2 * ratio);
        int g = (int) (g1 * invRatio + g2 * ratio);
        int b = (int) (b1 * invRatio + b2 * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ========== Getter方法 ==========
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return calculateWindowHeight();
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int windowHeight = calculateWindowHeight();
        return isMouseOver(mouseX, mouseY, x, y, width, windowHeight);
    }

    // ========== 静态方法 ==========
    public static void stopAllListening() {
        if (listeningKeybind != null) {
            listeningKeybind.stopListening();
            listeningKeybind = null;
        }
    }

    // 新增：获取当前正在监听的快捷键
    public static KeybindSetting getListeningKeybind() {
        return listeningKeybind;
    }

    // 在ClickGuiWindow类中添加以下方法
    // 设置窗口宽度
    public void setWidth(int width) {
        this.width = width;
    }

    // 检查是否有正在监听的快捷键
    public static boolean isListeningToKeybind() {
        return listeningKeybind != null;
    }
}