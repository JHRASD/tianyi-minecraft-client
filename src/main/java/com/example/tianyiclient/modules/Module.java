package com.example.tianyiclient.modules;

import com.example.tianyiclient.managers.KeybindManager;
import com.example.tianyiclient.settings.KeybindSetting;
import com.example.tianyiclient.settings.SettingGroup;
import com.example.tianyiclient.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;

    private boolean enabled = false;
    private int keybind = 0;
    private KeybindSetting keybindSetting = null;

    // 添加 MinecraftClient 实例
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final List<Setting<?>> settings = new ArrayList<>();
    private final SettingGroup settingGroup = new SettingGroup();

    // 防止重复初始化的标志
    private boolean initialized = false;

    // 添加：存储额外的显示信息
    private String displayInfo = null;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        // 自动初始化模块
        initModule();
    }

    /** 模块初始化方法 */
    private void initModule() {
        // 防止重复初始化
        if (initialized) return;

        // 先调用子类的初始化方法
        init();

        // 最后添加KeybindSetting（放在底部）
        addKeybindSettingToBottom();

        // 如果有默认按键，注册到KeybindManager
        if (keybind != 0) {
            KeybindManager.getInstance().registerKeybind(this, keybind);
        }

        initialized = true;
    }

    /** 子类的初始化方法，用于添加模块特定的设置 */
    protected void init() {
        // 子类重写此方法添加设置
    }

    /** 在设置列表底部添加快捷键设置 */
    private void addKeybindSettingToBottom() {
        // 检查是否已经添加过KeybindSetting
        boolean hasKeybindSetting = settings.stream()
                .anyMatch(s -> s instanceof KeybindSetting);

        // 如果没有，则创建并添加到列表末尾
        if (!hasKeybindSetting) {
            keybindSetting = new KeybindSetting("快捷键", "绑定模块快捷键", this);

            // 确保快捷键设置总是在列表末尾
            settings.add(keybindSetting);
            settingGroup.add(keybindSetting);
        }
    }

    /** 设置快捷键 */
    public void setKeybind(int key) {
        this.keybind = key;

        // 更新KeybindSetting的值
        if (keybindSetting != null) {
            keybindSetting.setValueDirect(key);
        }

        // 重新注册到KeybindManager
        KeybindManager.getInstance().registerKeybind(this, key);
    }

    /** 获取快捷键 */
    public int getKeybind() {
        return keybind;
    }

    /** 获取KeybindSetting */
    public KeybindSetting getKeybindSetting() {
        return keybindSetting;
    }

    /** 开启模块 */
    public void enable() {
        if (!enabled) {
            enabled = true;
            onEnable();
        }
    }

    /** 关闭模块 */
    public void disable() {
        if (enabled) {
            enabled = false;
            onDisable();
        }
    }

    /** 切换开关 */
    public void toggle() {
        if (enabled) disable();
        else enable();
    }

    /** 模块启用时调用 */
    protected void onEnable() {}

    /** 模块禁用时调用 */
    protected void onDisable() {}

    /** 每个 Tick 调用，子类可覆盖 */
    public void onTick() {}

    /**
     * 获取模块显示的额外信息（HUD上显示）
     * @return 要显示的信息，返回null或空字符串不显示
     */
    public String getInfo() {
        return displayInfo;
    }

    /**
     * 设置模块显示的额外信息
     * @param info 要显示的信息
     */
    protected void setDisplayInfo(String info) {
        this.displayInfo = info;
    }

    /**
     * 清除显示的额外信息
     */
    protected void clearDisplayInfo() {
        this.displayInfo = null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting<?>> getSettings() {
        // 确保KeybindSetting在最后
        List<Setting<?>> result = new ArrayList<>();

        // 先添加非KeybindSetting的设置
        for (Setting<?> setting : settings) {
            if (!(setting instanceof KeybindSetting)) {
                result.add(setting);
            }
        }

        // 最后添加KeybindSetting
        if (keybindSetting != null) {
            result.add(keybindSetting);
        }

        return result;
    }

    public SettingGroup getSettingGroup() {
        return settingGroup;
    }

    /** 添加设置 */
    public <T extends Setting<?>> T addSetting(T setting) {
        // 如果是KeybindSetting，使用特殊处理
        if (setting instanceof KeybindSetting) {
            // 如果已经存在keybindSetting，替换它
            if (keybindSetting != null) {
                settings.remove(keybindSetting);
                settingGroup.remove(keybindSetting);
            }
            keybindSetting = (KeybindSetting) setting;

            // KeybindSetting总是添加到列表末尾
            settings.add(keybindSetting);
            settingGroup.add(keybindSetting);
            return setting;
        }

        // 对于非KeybindSetting设置，检查是否已存在相同的设置
        boolean exists = settings.stream()
                .anyMatch(s -> s.getName().equals(setting.getName()) && !(s instanceof KeybindSetting));

        if (!exists) {
            // 确保添加到KeybindSetting之前
            if (keybindSetting != null && settings.contains(keybindSetting)) {
                int keybindIndex = settings.indexOf(keybindSetting);
                settings.add(keybindIndex, setting);
                settingGroup.add(setting);
            } else {
                settings.add(setting);
                settingGroup.add(setting);
            }
        }
        return setting;
    }

    /** 直接设置keybind（内部使用，避免循环调用） */
    public void setKeybindDirect(int key) {
        this.keybind = key;
        // 注意：这里不调用 keybindSetting.setValue，避免循环调用
    }

    /** 获取 MinecraftClient 实例 */
    public MinecraftClient getMinecraftClient() {
        return mc;
    }

    /** 根据名称获取设置项 */
    public Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equals(name)) {
                return setting;
            }
        }
        return null;
    }

    /** 获取布尔设置的值 */
    public boolean getBoolSettingValue(String name) {
        Setting<?> setting = getSettingByName(name);
        if (setting instanceof com.example.tianyiclient.settings.BoolSetting) {
            return ((com.example.tianyiclient.settings.BoolSetting) setting).getValue();
        }
        return false;
    }

    /** 获取Double设置的值 */
    public double getDoubleSettingValue(String name) {
        Setting<?> setting = getSettingByName(name);
        if (setting instanceof com.example.tianyiclient.settings.DoubleSetting) {
            return ((com.example.tianyiclient.settings.DoubleSetting) setting).getValue();
        }
        return 0.0;
    }

    /** 获取Integer设置的值 */
    public int getIntegerSettingValue(String name) {
        Setting<?> setting = getSettingByName(name);
        if (setting instanceof com.example.tianyiclient.settings.IntegerSetting) {
            return ((com.example.tianyiclient.settings.IntegerSetting) setting).getValue();
        }
        return 0;
    }
}