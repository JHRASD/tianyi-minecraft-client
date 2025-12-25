package com.example.tianyiclient.hud;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.hud.elements.WatermarkElement;
import com.example.tianyiclient.modules.render.HUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    // 存储所有HUD元素的列表
    private final List<HudElement> elements = new ArrayList<>();
    private boolean editing = false; // 是否处于编辑模式
    private HudElement selectedElement = null; // 当前被拖动的元素

    // 引用HUD模块
    private HUD hudModule = null;

    /**
     * 初始化HUD管理器 - 无参版本（兼容旧代码）
     */
    public void init() {
        // 使用无参初始化
        initInternal();
    }

    /**
     * 初始化HUD管理器 - 带HUD模块版本
     */
    public void init(HUD hudModule) {
        this.hudModule = hudModule;
        initInternal();
    }

    /**
     * 内部初始化方法
     */
    private void initInternal() {
        // 不需要使用 Fabric API 注册，因为我们已经通过 mixin 注入了
        // 只需要初始化元素

        // 注册默认的HUD元素
        registerElement(new WatermarkElement());

        TianyiClient.LOGGER.info("[HUD] 管理器初始化完成，已注册 {} 个元素", elements.size());
    }

    /**
     * 设置HUD模块引用
     */
    public void setHudModule(HUD hudModule) {
        this.hudModule = hudModule;
    }

    /**
     * 注册一个HUD元素
     */
    public void registerElement(HudElement element) {
        if (element != null) {
            elements.add(element);
            TianyiClient.LOGGER.debug("[HUD] 注册元素: {}", element.getName());
        }
    }

    /**
     * HUD渲染入口 - 由HudRenderer调用
     */
    public void renderHud(DrawContext context, float tickDelta) {
        if (context == null) {
            return;
        }

        // === 检查HUD模块状态 ===
        if (hudModule != null && !hudModule.isEnabled()) {
            return;
        }

        // 渲染所有启用的HUD元素
        for (HudElement element : elements) {
            try {
                // 检查水印是否应该显示
                if (element instanceof WatermarkElement) {
                    if (hudModule != null && !hudModule.shouldShowWatermark()) {
                        continue;
                    }
                }

                // 其他元素正常渲染
                if (element.isEnabled()) {
                    element.render(context, tickDelta);
                }
            } catch (Exception e) {
                TianyiClient.LOGGER.error("[HUD] 渲染元素 {} 时出错: {}", element.getName(), e.getMessage());
                e.printStackTrace();
            }
        }

        // 如果处于编辑模式，渲染编辑辅助UI（旧模式）
        if (editing) {
            renderEditOverlay(context);
        }
    }

    /**
     * 渲染编辑模式下的辅助UI（旧模式）
     */
    private void renderEditOverlay(DrawContext context) {
        if (!editing) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        // 如果HUD模块被禁用，不显示编辑模式
        if (hudModule != null && !hudModule.isEnabled()) {
            return;
        }

        // 检查是否在新编辑器屏幕中，如果是则不显示旧UI
        if (client.currentScreen != null &&
                client.currentScreen.getClass().getName().contains("HudEditorScreen")) {
            return;
        }

        // 显示编辑模式提示（旧模式）
        String hint = "HUD编辑模式 (简单模式)";
        context.drawText(client.textRenderer, hint, 10, 10, 0xFFFFFF, true);

        // 高亮当前选中的元素
        if (selectedElement != null && selectedElement.isEnabled()) {
            int x = (int) selectedElement.getX();
            int y = (int) selectedElement.getY();
            int width = (int) selectedElement.getWidth();
            int height = (int) selectedElement.getHeight();

            // 绘制黄色边框
            HudRenderer.drawRect(context, x - 1, y - 1, width + 2, 1, 0xFFFFFF00); // 上边框
            HudRenderer.drawRect(context, x - 1, y + height, width + 2, 1, 0xFFFFFF00); // 下边框
            HudRenderer.drawRect(context, x - 1, y, 1, height, 0xFFFFFF00); // 左边框
            HudRenderer.drawRect(context, x + width, y, 1, height, 0xFFFFFF00); // 右边框
        }
    }

    /**
     * 检查是否可以编辑HUD
     */
    public boolean canEdit() {
        return hudModule == null || hudModule.isEnabled();
    }

    /**
     * 切换HUD编辑模式
     */
    public void toggleEditMode() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // 如果HUD模块被禁用，不能进入编辑模式
        if (hudModule != null && !hudModule.isEnabled()) {
            if (client.player != null) {
                client.player.sendMessage(
                        net.minecraft.text.Text.literal("§c请先启用HUD模块才能编辑"), false);
            }
            return;
        }

        // 检查是否已经在新编辑器屏幕中
        if (client.currentScreen != null &&
                client.currentScreen.getClass().getName().contains("HudEditorScreen")) {
            // 已经在编辑器中，关闭它
            client.setScreen(null);
            editing = false;
            selectedElement = null;

            if (client.player != null) {
                client.player.sendMessage(net.minecraft.text.Text.literal("§cHUD编辑器已关闭"), false);
            }
            TianyiClient.LOGGER.info("[HUD] HUD编辑器已关闭");
            return;
        }

        // 尝试打开新的编辑器
        try {
            Class<?> editorClass = Class.forName("com.example.tianyiclient.gui.hud.HudEditorScreen");
            Object editorInstance = editorClass.getDeclaredConstructor().newInstance();

            // 打开编辑器屏幕
            client.setScreen((net.minecraft.client.gui.screen.Screen) editorInstance);
            editing = true; // 标记为编辑模式

            if (client.player != null) {
                client.player.sendMessage(net.minecraft.text.Text.literal("§aHUD编辑器已打开"), false);
            }
            TianyiClient.LOGGER.info("[HUD] HUD编辑器已打开");

        } catch (Exception e) {
            // 如果新的编辑器加载失败，使用旧的简单编辑模式
            System.out.println("无法加载HUD编辑器，使用旧模式: " + e.getMessage());

            // 旧的编辑模式逻辑
            editing = !editing;
            selectedElement = null;

            if (client.player != null) {
                String message = editing ? "§aHUD编辑模式已开启" : "§cHUD编辑模式已关闭";
                client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
                TianyiClient.LOGGER.info("[HUD] {}", message.replace("§a", "").replace("§c", ""));
            }
        }
    }

    /**
     * 处理鼠标点击（用于编辑模式下选中和拖动元素）
     */
    public void onMouseClick(double mouseX, double mouseY, int button) {
        // 如果不在编辑模式，不处理
        if (!editing || (hudModule != null && !hudModule.isEnabled())) return;

        // 检查是否在新编辑器屏幕中
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen != null &&
                client.currentScreen.getClass().getName().contains("HudEditorScreen")) {
            return; // 在新编辑器中，不在这里处理
        }

        if (button == 0) { // 左键点击
            // 从最上面的元素开始检测
            for (int i = elements.size() - 1; i >= 0; i--) {
                HudElement element = elements.get(i);
                if (element.isEnabled() && element.isMouseOver(mouseX, mouseY)) {
                    selectedElement = element;
                    element.startDragging(mouseX, mouseY);
                    return;
                }
            }
            selectedElement = null; // 点击空白处取消选择
        }
    }

    /**
     * 处理鼠标释放
     */
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        // 如果不在编辑模式，不处理
        if (!editing || selectedElement == null || (hudModule != null && !hudModule.isEnabled())) return;

        // 检查是否在新编辑器屏幕中
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen != null &&
                client.currentScreen.getClass().getName().contains("HudEditorScreen")) {
            return; // 在新编辑器中，不在这里处理
        }

        if (button == 0) {
            selectedElement.stopDragging();
        }
    }

    /**
     * 处理鼠标拖动
     */
    public void onMouseDrag(double mouseX, double mouseY) {
        // 如果不在编辑模式，不处理
        if (!editing || selectedElement == null || (hudModule != null && !hudModule.isEnabled())) return;

        // 检查是否在新编辑器屏幕中
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen != null &&
                client.currentScreen.getClass().getName().contains("HudEditorScreen")) {
            return; // 在新编辑器中，不在这里处理
        }

        selectedElement.updateDrag(mouseX, mouseY);
    }

    // ========== 公开方法 ==========

    public List<HudElement> getElements() {
        return new ArrayList<>(elements);
    }

    public boolean isEditing() {
        return editing && (hudModule == null || hudModule.isEnabled());
    }

    public HudElement getSelectedElement() {
        return selectedElement;
    }

    /**
     * 获取HUD模块
     */
    public HUD getHudModule() {
        return hudModule;
    }
}