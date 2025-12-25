package com.example.tianyiclient.gui.hud;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD可视化编辑器
 */
public class HudEditorScreen extends Screen {
    private final HudManager hudManager;
    private HudElement selectedElement;
    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;
    private long lastClickTime = 0;

    // 编辑状态
    private List<HudElement> elementsInEditor = new ArrayList<>();

    public HudEditorScreen() {
        super(Text.literal("HUD编辑器"));
        this.hudManager = TianyiClient.getInstance().getHudManager();

        // 复制HUD元素到编辑器列表
        if (hudManager != null) {
            elementsInEditor.addAll(hudManager.getElements());
        }
    }

    @Override
    protected void init() {
        super.init();

        // 顶部按钮栏
        addTopButtons();
    }

    /**
     * 添加上方控制按钮
     */
    private void addTopButtons() {
        int buttonWidth = 80;
        int buttonHeight = 20;
        int startX = width / 2 - (buttonWidth * 3 + 20) / 2;
        int y = 5;

        // 保存按钮
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("保存"),
                        button -> saveLayout()
                )
                .dimensions(startX, y, buttonWidth, buttonHeight)
                .build());

        // 关闭按钮
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("关闭"),
                        button -> closeEditor()
                )
                .dimensions(startX + buttonWidth * 2 + 20, y, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. 先绘制纯黑色背景（完全遮盖游戏）
        renderSolidBackground(context);

        // 2. 渲染所有HUD元素（在编辑器模式下）
        renderHudElementsInEditor(context, delta);

        // 3. 渲染选中元素的边框
        if (selectedElement != null) {
            renderSelectionBox(context, selectedElement);
        }

        // 4. 渲染编辑器UI（标题、提示等）
        renderEditorUI(context);

        // 5. 渲染按钮等组件
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * 渲染纯色背景（完全遮盖游戏）
     */
    private void renderSolidBackground(DrawContext context) {
        int bgColor = 0xFF202020; // 深灰色背景
        context.fill(0, 0, width, height, bgColor);

        // 添加一些装饰线
        int lineColor = 0xFF333333;
        for (int i = 0; i < width; i += 20) {
            context.fill(i, 0, i + 1, height, lineColor);
        }
        for (int i = 0; i < height; i += 20) {
            context.fill(0, i, width, i + 1, lineColor);
        }
    }

    /**
     * 渲染编辑器中的HUD元素
     */
    private void renderHudElementsInEditor(DrawContext context, float delta) {
        for (HudElement element : elementsInEditor) {
            if (!element.isEnabled()) continue;

            // 保存原始可见性状态
            boolean originalVisible = element.isVisible();

            // 强制在编辑器中可见
            element.setVisible(true);

            // 渲染元素
            element.render(context, delta);

            // 恢复原始状态
            element.setVisible(originalVisible);
        }
    }

    /**
     * 渲染选中元素的边框
     */
    private void renderSelectionBox(DrawContext context, HudElement element) {
        int x = (int) element.getX();
        int y = (int) element.getY();
        int elementWidth = (int) element.getWidth();
        int elementHeight = (int) element.getHeight();

        int borderColor = 0xFFFF00FF; // 洋红色边框（更明显）
        int cornerColor = 0xFF00FFFF; // 青色角点

        // 绘制粗边框（2像素宽）
        context.fill(x - 2, y - 2, x + elementWidth + 2, y, borderColor); // 上边
        context.fill(x - 2, y + elementHeight, x + elementWidth + 2, y + elementHeight + 2, borderColor); // 下边
        context.fill(x - 2, y, x, y + elementHeight, borderColor); // 左边
        context.fill(x + elementWidth, y, x + elementWidth + 2, y + elementHeight, borderColor); // 右边

        // 绘制4个角点
        int cornerSize = 6;
        context.fill(x - cornerSize, y - cornerSize, x, y, cornerColor); // 左上
        context.fill(x + elementWidth, y - cornerSize, x + elementWidth + cornerSize, y, cornerColor); // 右上
        context.fill(x - cornerSize, y + elementHeight, x, y + elementHeight + cornerSize, cornerColor); // 左下
        context.fill(x + elementWidth, y + elementHeight, x + elementWidth + cornerSize, y + elementHeight + cornerSize, cornerColor); // 右下

        // 显示元素信息
        String info = element.getName() + " (" + (int)element.getX() + ", " + (int)element.getY() + ")";
        context.drawText(textRenderer, info, x + elementWidth + 10, y, 0xFFFFFF, true);
    }

    /**
     * 渲染编辑器UI
     */
    private void renderEditorUI(DrawContext context) {
        // 标题
        String title = "✏️ HUD编辑器";
        context.drawText(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 30, 0xFFFFFF, true);

        // 显示选中信息
        if (selectedElement != null) {
            String selectedInfo = "当前选中: " + selectedElement.getName();
            context.drawText(textRenderer, selectedInfo, 10, height - 40, 0x00FF00, true);
        }

        // 帮助提示
        String help1 = "左键点击选择元素";
        String help2 = "左键拖动移动元素";
        String help3 = "ESC键关闭编辑器";

        context.drawText(textRenderer, help1, 10, height - 80, 0xCCCCCC, true);
        context.drawText(textRenderer, help2, 10, height - 70, 0xCCCCCC, true);
        context.drawText(textRenderer, help3, 10, height - 60, 0xCCCCCC, true);

        // 显示元素统计
        long enabledCount = elementsInEditor.stream().filter(HudElement::isEnabled).count();
        String stats = "总元素: " + elementsInEditor.size() + " | 已启用: " + enabledCount;
        context.drawText(textRenderer, stats, width - textRenderer.getWidth(stats) - 10, 30, 0xAAAAAA, true);
    }

    // ========== 鼠标交互 ==========

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        long currentTime = System.currentTimeMillis();
        boolean isDoubleClick = (currentTime - lastClickTime < 300);
        lastClickTime = currentTime;

        // 左键：选择或开始拖动
        if (button == 0) {
            // 检查是否点击了HUD元素
            HudElement clickedElement = getElementAt(mouseX, mouseY);

            if (clickedElement != null) {
                selectedElement = clickedElement;

                // 如果是双击，可以添加更多功能（比如打开设置）
                if (isDoubleClick) {
                    System.out.println("双击元素: " + clickedElement.getName());
                    // 可以在这里添加打开设置面板的逻辑
                }

                // 开始拖动
                dragging = true;
                dragOffsetX = (float) (mouseX - selectedElement.getX());
                dragOffsetY = (float) (mouseY - selectedElement.getY());

                System.out.println("选中并开始拖动元素: " + clickedElement.getName());
                return true;
            } else {
                // 点击空白处，取消选择
                selectedElement = null;
                dragging = false;
                System.out.println("取消选择");
            }
        }

        // 右键：如果选中了元素，可以添加右键菜单功能
        if (button == 1 && selectedElement != null) {
            System.out.println("右键点击元素: " + selectedElement.getName());
            // 可以在这里添加快捷菜单（启用/禁用、删除等）
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging && selectedElement != null) {
            dragging = false;

            // 保存位置到设置
            selectedElement.updateSettingsFromPosition();

            System.out.println("停止拖动，位置已保存: " + selectedElement.getName());
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragging && selectedElement != null) {
            // 计算新位置
            float newX = (float) (mouseX - dragOffsetX);
            float newY = (float) (mouseY - dragOffsetY);

            // 确保在屏幕范围内
            newX = Math.max(0, Math.min(newX, width - selectedElement.getWidth()));
            newY = Math.max(0, Math.min(newY, height - selectedElement.getHeight()));

            // 更新元素位置
            selectedElement.setPosition(newX, newY);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // ========== 辅助方法 ==========

    /**
     * 获取指定位置的元素
     */
    private HudElement getElementAt(double mouseX, double mouseY) {
        // 从最上层开始检测（反向遍历）
        for (int i = elementsInEditor.size() - 1; i >= 0; i--) {
            HudElement element = elementsInEditor.get(i);
            if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
                return element;
            }
        }
        return null;
    }

    /**
     * 保存布局
     */
    private void saveLayout() {
        if (hudManager == null) return;

        // 保存每个元素的位置
        for (HudElement element : elementsInEditor) {
            element.updateSettingsFromPosition();
        }

        // 显示消息
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§a✅ HUD布局已保存"), false);
        }

        System.out.println("HUD布局已保存");
    }

    /**
     * 关闭编辑器
     */
    private void closeEditor() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭编辑器
        if (keyCode == 256) { // ESC
            closeEditor();
            return true;
        }

        // S键保存
        if (keyCode == 83) { // S
            saveLayout();
            return true;
        }

        // 空格键切换选中元素的启用状态
        if (keyCode == 32 && selectedElement != null) { // 空格
            selectedElement.setEnabled(!selectedElement.isEnabled());
            System.out.println("切换元素状态: " + selectedElement.getName() + " -> " +
                    (selectedElement.isEnabled() ? "启用" : "禁用"));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false; // 编辑器不暂停游戏
    }
}