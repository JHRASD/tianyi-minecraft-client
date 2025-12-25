package com.example.tianyiclient.hud;

import com.example.tianyiclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class HudElement {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    protected final String name;
    protected boolean enabled = true;
    protected float x, y;
    protected float width, height;
    protected boolean dragging = false;
    protected float dragOffsetX, dragOffsetY;

    // === 使用你的 SettingGroup 类 ===
    // 注意：你的SettingGroup没有构造函数参数
    protected final SettingGroup generalGroup = new SettingGroup();
    protected final SettingGroup styleGroup = new SettingGroup();
    protected final SettingGroup layoutGroup = new SettingGroup();
    protected final List<SettingGroup> settingGroups = new ArrayList<>();

    // 所有设置的扁平化列表（兼容旧代码）
    protected final List<Setting<?>> allSettings = new ArrayList<>();

    // 布局属性
    protected float scale = 1.0f;
    protected float alpha = 1.0f;
    protected boolean visible = true;

    public HudElement(String name, float defaultX, float defaultY) {
        this.name = name;
        this.x = defaultX;
        this.y = defaultY;

        // 初始化设置组列表
        settingGroups.add(generalGroup);
        settingGroups.add(styleGroup);
        settingGroups.add(layoutGroup);

        // === 初始化通用设置 ===
        // 使用你的SettingGroup的add()方法
        generalGroup.add(new BoolSetting("启用", name + " - 启用", true));
        generalGroup.add(new DoubleSetting("X", name + " - X", (double) defaultX, 0.0, 2000.0));
        generalGroup.add(new DoubleSetting("Y", name + " - Y", (double) defaultY, 0.0, 2000.0));

        // === 初始化布局设置 ===
        layoutGroup.add(new DoubleSetting("缩放", name + " - 缩放", 1.0, 0.1, 5.0));
        layoutGroup.add(new DoubleSetting("透明度", name + " - 透明度", 1.0, 0.0, 1.0));

        // 将所有设置合并到扁平化列表
        for (SettingGroup group : settingGroups) {
            allSettings.addAll(group.getSettings());
        }
    }

    public abstract void render(DrawContext context, float tickDelta);

    /**
     * 从设置同步状态到元素属性
     */
    public void updateFromSettings() {
        // 同步通用设置
        this.enabled = getSettingValue(generalGroup, "启用", Boolean.class, true);
        Double xValue = getSettingValue(generalGroup, "X", Double.class, (double) x);
        Double yValue = getSettingValue(generalGroup, "Y", Double.class, (double) y);
        if (xValue != null) this.x = xValue.floatValue();
        if (yValue != null) this.y = yValue.floatValue();

        // 同步布局设置
        Double scaleValue = getSettingValue(layoutGroup, "缩放", Double.class, (double) scale);
        Double alphaValue = getSettingValue(layoutGroup, "透明度", Double.class, (double) alpha);
        if (scaleValue != null) this.scale = scaleValue.floatValue();
        if (alphaValue != null) this.alpha = alphaValue.floatValue();
    }

    /**
     * 将元素的当前位置同步回设置
     */
    public void updateSettingsFromPosition() {
        updateSettingValue(generalGroup, "X", (double) x);
        updateSettingValue(generalGroup, "Y", (double) y);
    }

    // === 便捷的设置值获取方法 ===
    @SuppressWarnings("unchecked")
    protected <T> T getSettingValue(SettingGroup group, String settingName, Class<T> type, T defaultValue) {
        for (Setting<?> setting : group.getSettings()) {
            // 使用getDescription()查找包含设置名的设置
            if (setting.getDescription() != null && setting.getDescription().contains(settingName)) {
                Object value = setting.getValue();
                if (value != null && type.isInstance(value)) {
                    return (T) value;
                }
            }
        }
        return defaultValue;
    }

    // === 安全更新设置值的方法 ===
    protected void updateSettingValue(SettingGroup group, String settingName, Object value) {
        for (Setting<?> setting : group.getSettings()) {
            if (setting.getDescription() != null && setting.getDescription().contains(settingName)) {
                try {
                    // 根据Setting的类型安全地设置值
                    if (setting instanceof BoolSetting && value instanceof Boolean) {
                        ((BoolSetting) setting).setValue((Boolean) value);
                    } else if (setting instanceof DoubleSetting && value instanceof Double) {
                        ((DoubleSetting) setting).setValue((Double) value);
                    } else if (setting instanceof IntegerSetting && value instanceof Integer) {
                        ((IntegerSetting) setting).setValue((Integer) value);
                    } else if (setting instanceof StringSetting && value instanceof String) {
                        ((StringSetting) setting).setValue((String) value);
                    } else if (setting instanceof ColorSetting && value instanceof Integer) {
                        ((ColorSetting) setting).setValue((Integer) value);
                    }
                    // 可以添加其他Setting类型的处理
                } catch (Exception e) {
                    System.err.println("更新设置值时出错: " + settingName + ", 错误: " + e.getMessage());
                }
                return;
            }
        }
    }

    // === 鼠标交互方法 ===
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void startDragging(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = (float) (mouseX - x);
        dragOffsetY = (float) (mouseY - y);
    }

    public void stopDragging() {
        if (dragging) {
            dragging = false;
            updateSettingsFromPosition();
        }
    }

    public void updateDrag(double mouseX, double mouseY) {
        if (dragging) {
            x = (float) (mouseX - dragOffsetX);
            y = (float) (mouseY - dragOffsetY);
        }
    }

    // === Getter & Setter ===
    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateSettingValue(generalGroup, "启用", enabled);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    protected void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    // 获取所有设置（扁平化列表，兼容旧代码）
    public List<Setting<?>> getSettings() {
        return new ArrayList<>(allSettings);
    }

    // 获取设置组（用于新的编辑器UI）
    public List<SettingGroup> getSettingGroups() {
        return new ArrayList<>(settingGroups);
    }

    // 在 HudElement.java 类中添加以下方法：

    /**
     * 设置元素位置
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateSettingsFromPosition(); // 同步到设置
    }

    /**
     * 检查元素是否应该渲染（编辑器中需要）
     */
    public boolean shouldRenderInEditor() {
        return isVisible();
    }

    // 如果还没有这个方法，添加：
    public void setPositionDirect(float x, float y) {
        this.x = x;
        this.y = y;
        updateSettingsFromPosition();
        // 不更新设置（用于临时拖动）
    }

    // 获取特定设置组
    public SettingGroup getGeneralGroup() { return generalGroup; }
    public SettingGroup getStyleGroup() { return styleGroup; }
    public SettingGroup getLayoutGroup() { return layoutGroup; }

    public float getScale() { return scale; }
    public float getAlpha() { return alpha; }
    public boolean isVisible() { return visible && enabled; }
    public void setVisible(boolean visible) { this.visible = visible; }

    /**
     * 旧的兼容方法 - 通过描述查找设置
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSettingByDescription(String descriptionPart, Class<T> type) {
        for (Setting<?> setting : allSettings) {
            if (setting.getDescription().contains(descriptionPart)) {
                Object value = setting.getValue();
                if (type.isInstance(value)) {
                    return (T) value;
                }
            }
        }
        return null;
    }
}