package com.example.tianyiclient.gui;

import com.example.tianyiclient.modules.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGUI extends net.minecraft.client.gui.screen.Screen {
    // 颜色配置
    public static class Colors {
        public static final int BACKGROUND = 0x901A1A1A; // 半透明背景
        public static final int TITLE_BG = 0xFF000000;
        public static final int MODULE_HOVER = 0xFF69B4FF;
        public static final int MODULE_ENABLED = 0xFF6C35DE;
        public static final int SETTING_BG = 0xFF282828;
        public static final int SETTING_HOVER = 0xFF3C3C3C;
        public static final int ACCENT = 0xFF6C35DE;
        public static final int TEXT = 0xFFFFFFFF;
        public static final int TEXT_SECONDARY = 0xFFE6E6E6;
        public static final int SLIDER_TRACK = 0xFF4D4D4D;
        public static final int TEXT_FIELD_BG = 0xFF321428;
        public static final int WINDOW_DIVIDER = 0xFF111111; // 窗口分隔线颜色
    }

    // 窗口列表
    private final List<ClickGuiWindow> windows = new ArrayList<>();
    private boolean initialized = false;

    // 动画状态
    private float globalAlpha = 0f;
    private boolean closing = false;

    // 快捷键监听状态
    private static boolean isListeningForKeybind = false;

    public ClickGUI() {
        super(Text.literal("天依客户端"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 动画更新
        updateAnimations(delta);

        // 渲染半透明背景
        context.fill(0, 0, this.width, this.height, Colors.BACKGROUND);

        // 初始化窗口（只在第一次渲染时）
        if (!initialized) {
            initWindows();
            initialized = true;
        }

        // 渲染所有窗口
        for (ClickGuiWindow window : windows) {
            window.render(context, mouseX, mouseY, delta);
        }

        // 在窗口之间添加分隔线（在所有窗口渲染完成后）
        if (windows.size() > 1) {
            renderWindowDividers(context);
        }

        // 渲染底部信息（如果GUI完全显示）
        if (globalAlpha > 0.8f) {
            renderBottomInfo(context);
        }
    }

    // 新增方法：渲染窗口分隔线
    private void renderWindowDividers(DrawContext context) {
        for (int i = 0; i < windows.size() - 1; i++) {
            ClickGuiWindow leftWindow = windows.get(i);
            ClickGuiWindow rightWindow = windows.get(i + 1);

            int dividerX = leftWindow.getX() + leftWindow.getWidth();
            int dividerY = leftWindow.getY();
            int dividerHeight = Math.max(leftWindow.getHeight(), rightWindow.getHeight());

            // 绘制深色分隔线
            context.fill(dividerX, dividerY, dividerX + 1, dividerY + dividerHeight, Colors.WINDOW_DIVIDER);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 空实现，避免Minecraft渲染默认背景
    }

    private void updateAnimations(float delta) {
        if (closing) {
            globalAlpha = MathHelper.lerp(0.3f, globalAlpha, 0f);
            if (globalAlpha < 0.05f) {
                this.client.setScreen(null);
            }
        } else {
            globalAlpha = MathHelper.lerp(0.3f, globalAlpha, 1f);
        }
    }

    private void initWindows() {
        windows.clear();

        Category[] categories = Category.values();

        // 使用实际屏幕宽度，而不是固定值
        int screenWidth = this.width;
        int categoriesCount = categories.length;

        // 计算每个窗口的理想宽度
        // 1400宽度 ÷ 6 = 233，但我们希望紧凑一些，用108px，留出间距
        int windowWidth = 108; // 稍微减小宽度，留出间距

        // 窗口之间的间距
        int spacing = 2; // 2像素间距

        // 计算总宽度：窗口宽度 × 数量 + 间距 × (数量 - 1)
        int totalWidth = categoriesCount * windowWidth + (categoriesCount - 1) * spacing;

        // 如果总宽度超过屏幕，进一步减小窗口宽度
        if (totalWidth > screenWidth) {
            // 重新计算，确保所有窗口都能放下
            windowWidth = (screenWidth - (categoriesCount - 1) * spacing) / categoriesCount;
            totalWidth = categoriesCount * windowWidth + (categoriesCount - 1) * spacing;
        }

        // 从最左边开始 (x=0)
        int startX = 0;
        int startY = 20;

        for (int i = 0; i < categories.length; i++) {
            // 创建窗口并设置宽度
            ClickGuiWindow window = new ClickGuiWindow(
                    startX + i * (windowWidth + spacing),
                    startY,
                    categories[i]
            );

            // 动态设置窗口宽度
            window.setWidth(windowWidth);
            windows.add(window);
        }

        // 调试输出：显示窗口布局信息
        System.out.println("屏幕宽度: " + screenWidth);
        System.out.println("窗口数量: " + categoriesCount);
        System.out.println("每个窗口宽度: " + windowWidth);
        System.out.println("窗口间距: " + spacing);
        System.out.println("总宽度: " + totalWidth);
    }

    private void renderBottomInfo(DrawContext context) {
        String info = "左键:开关 │ 右键:展开设置 │ 拖拽标题栏:移动窗口 │ ESC:关闭";
        int textY = this.height - 15;
        int textColor = (int)(globalAlpha * 255) << 24 | 0xFFFFFF;
        context.drawCenteredTextWithShadow(this.textRenderer, info, this.width / 2, textY, textColor);

        // 如果正在监听快捷键，显示提示
        if (isListeningForKeybind) {
            String hint = "按下按键绑定快捷键，ESC取消，DEL清除";
            int hintY = textY - 15;
            context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, hintY, 0xFFFFAA00);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 从后往前处理（最上面的窗口最先处理）
        for (int i = windows.size() - 1; i >= 0; i--) {
            ClickGuiWindow window = windows.get(i);
            if (window.isMouseOver((int)mouseX, (int)mouseY)) {
                if (window.onClicked((int)mouseX, (int)mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ClickGuiWindow window : windows) {
            window.onMouseReleased();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (ClickGuiWindow window : windows) {
            window.onDragged((int)mouseX, (int)mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 检查ESC键关闭
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // 如果有窗口正在监听快捷键，先停止监听
            if (isListeningForKeybind) {
                ClickGuiWindow.stopAllListening();
                isListeningForKeybind = false;
                return true;
            }

            // 否则关闭GUI
            closing = true;
            return true;
        }

        // 将按键事件传递给所有窗口
        boolean handled = false;
        for (ClickGuiWindow window : windows) {
            if (window.onKeyPressed(keyCode, scanCode, modifiers)) {
                handled = true;
                // 检查按键监听状态
                isListeningForKeybind = ClickGuiWindow.isListeningToKeybind();
                break;
            }
        }

        if (handled) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        // 停止所有快捷键监听
        ClickGuiWindow.stopAllListening();
        isListeningForKeybind = false;
        closing = true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // 我们手动处理 ESC 键
    }

    // 更新快捷键监听状态
    public static void updateKeybindListeningState(boolean listening) {
        isListeningForKeybind = listening;
    }

    // 新增方法：检查是否有正在监听的快捷键
    public static boolean isListeningToKeybind() {
        return isListeningForKeybind;
    }
}