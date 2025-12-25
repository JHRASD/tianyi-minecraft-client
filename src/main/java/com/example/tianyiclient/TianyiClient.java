package com.example.tianyiclient;

import com.example.tianyiclient.gui.ClickGUI;
import com.example.tianyiclient.hud.HudManager;
import com.example.tianyiclient.hud.elements.*;
import com.example.tianyiclient.managers.ModuleManager;
import com.example.tianyiclient.managers.KeybindManager;
import com.example.tianyiclient.modules.combat.AutoTotem;
import com.example.tianyiclient.modules.impl.hud.HudEditModule;
import com.example.tianyiclient.modules.misc.MiddleClickItem;
import com.example.tianyiclient.modules.movement.Flight;
import com.example.tianyiclient.modules.render.Fullbright;
import com.example.tianyiclient.modules.render.EntityInfoModule;
import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.modules.render.HUD;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TianyiClient implements ClientModInitializer {
    // 单例
    public static TianyiClient INSTANCE;

    // 全局事件总线
    public static final EventBus EVENT_BUS = EventBus.getInstance();

    // 管理器
    public ModuleManager moduleManager;
    public KeybindManager keybindManager;
    public HudManager hudManager;

    // HUD模块引用
    public HUD hudModule;

    // 日志
    public static final Logger LOGGER = LoggerFactory.getLogger("TianyiClient");

    // GUI 打开按键绑定
    private KeyBinding openGuiKey;

    /**
     * Fabric 客户端初始化方法
     */
    @Override
    public void onInitializeClient() {
        LOGGER.info("TianyiClient 开始初始化...");

        try {
            // 0. 设置单例实例
            INSTANCE = this;

            // 1. 先创建HUD模块
            this.hudModule = new HUD();

            // 2. 创建HUD管理器并传入HUD模块
            this.hudManager = new HudManager();
            this.hudManager.setHudModule(this.hudModule); // 简单连接

            // 3. 初始化模块管理器
            this.moduleManager = new ModuleManager();

            // 4. 初始化按键管理器
            this.keybindManager = KeybindManager.getInstance();

            // 5. 注册按键绑定
            registerKeybinds();

            // 6. 初始化所有系统
            init();

            // 7. 注册按键检测
            registerKeyDetection();

            LOGGER.info("TianyiClient 初始化完成");
        } catch (Exception e) {
            LOGGER.error("TianyiClient 初始化失败", e);
        }
    }

    /**
     * 注册按键绑定
     */
    private void registerKeybinds() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tianyiclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.tianyiclient.general"
        ));

        LOGGER.info("注册GUI打开按键: 右Shift");
    }

    /**
     * 注册按键检测
     */
    private void registerKeyDetection() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 检查GUI打开按键
            if (openGuiKey.wasPressed()) {
                if (client != null) {
                    LOGGER.info("打开GUI按键被按下");
                    client.setScreen(new ClickGUI());
                }
            }
        });
    }

    /**
     * 初始化客户端框架
     */
    private void init() {
        // 1. 初始化快捷键管理器
        keybindManager.init();

        // 2. 初始化HUD管理器
        if (hudManager != null) {
            hudManager.init(); // 现在使用无参版本
            LOGGER.info("HUD管理器初始化完成");
        }

        // 3. 初始化模块管理器
        moduleManager.init();

        // 4. 注册模块和HUD元素
        initModulesAndElements();

        // 5. 注册事件
        initEvents();

        // 6. GUI初始化
        initGUI();

        // 7. 启动EventBus测试
        startEventBusTest();
    }

    /**
     * 启动EventBus测试（仅开发模式）
     */
    private void startEventBusTest() {
        try {
            Class<?> testClass = Class.forName("com.example.tianyiclient.test.EventBusTest");
            testClass.getMethod("startTest").invoke(null);
            LOGGER.info("EventBus测试已启动");
        } catch (Exception e) {
            LOGGER.info("EventBus测试未启用");
        }
    }

    /**
     * 初始化模块和HUD元素
     */
    private void initModulesAndElements() {
        // 先注册HUD模块
        if (hudModule != null) {
            moduleManager.register(hudModule);
            LOGGER.info("注册HUD模块成功");
        }

        // 【重要】注册所有HUD元素
        if (hudManager != null) {
            // 1. 水印元素
            hudManager.registerElement(new WatermarkElement());
            LOGGER.info("注册水印HUD元素成功");
            //网络信息
            hudManager.registerElement(new NetworkInfoElement());
            LOGGER.info("注册网络信息HUD元素成功");

            // 2. FPS信息元素
            hudManager.registerElement(new FpsElement());
            LOGGER.info("注册FPS信息元素成功");

            // 3. 坐标信息元素
            hudManager.registerElement(new CoordinatesElement());
            LOGGER.info("注册坐标信息元素成功");

            // 4. 模块列表元素
            hudManager.registerElement(new ModuleListElement());
            LOGGER.info("注册模块列表元素成功");

            // 5. 玩家状态元素
            hudManager.registerElement(new PlayerStatusElement());
            LOGGER.info("注册玩家状态元素成功");

            // 6. 性能监控元素（新添加）
            hudManager.registerElement(new PerformanceMonitorElement());
            LOGGER.info("注册性能监控HUD元素成功");

            // 7. 敌人状态元素（新添加）
            hudManager.registerElement(new EnemyStatusElement());
            LOGGER.info("注册敌人状态HUD元素成功");

            // 8. 游戏信息元素（新添加）
            hudManager.registerElement(new GameInfoElement());
            LOGGER.info("注册游戏信息HUD元素成功");

            LOGGER.info("共注册了 {} 个HUD元素", hudManager.getElements().size());
        }

        // 注册实体信息模块
        EntityInfoModule entityInfoModule = new EntityInfoModule();
        moduleManager.register(entityInfoModule);
        LOGGER.info("注册实体信息模块成功");

        // 注册对应的HUD元素
        if (hudManager != null) {
            hudManager.registerElement(new EntityInfoElement(entityInfoModule));
            LOGGER.info("注册实体信息HUD元素成功");
        }

        // 添加其他模块
        moduleManager.register(new Flight());
        moduleManager.register(new Fullbright());
        moduleManager.register(new AutoTotem());
        moduleManager.register(new MiddleClickItem());


        // HudEditModule需要hudManager
        if (hudManager != null) {
            moduleManager.register(new HudEditModule(hudManager));
            LOGGER.info("注册HUD编辑模块成功");
        }

        // 尝试添加测试模块（如果存在）
        try {
            // 测试模块
            Class<?> testModuleClass = Class.forName("com.example.tianyiclient.modules.test.TestModule");
            com.example.tianyiclient.modules.Module testModule =
                    (com.example.tianyiclient.modules.Module) testModuleClass.getDeclaredConstructor().newInstance();
            moduleManager.register(testModule);
            LOGGER.info("注册测试模块成功");

            // 调试模块
            Class<?> debugModuleClass = Class.forName("com.example.tianyiclient.modules.test.DebugModule");
            com.example.tianyiclient.modules.Module debugModule =
                    (com.example.tianyiclient.modules.Module) debugModuleClass.getDeclaredConstructor().newInstance();
            moduleManager.register(debugModule);
            LOGGER.info("注册调试模块成功");
        } catch (Exception e) {
            LOGGER.warn("无法注册测试模块: {}", e.getMessage());
        }

        LOGGER.info("注册了 {} 个模块", moduleManager.getModules().size());
    }

    /**
     * 注册事件监听器
     */
    private void initEvents() {
        // 模块管理器本身可以监听事件
        EVENT_BUS.register(moduleManager);

        // 注册各个模块到事件总线
        moduleManager.getModules().forEach(module -> {
            EVENT_BUS.register(module);
        });

        // 注册HUD管理器到事件总线
        if (hudManager != null) {
            EVENT_BUS.register(hudManager);
        }
    }

    /**
     * 初始化 GUI
     */
    private void initGUI() {
        LOGGER.info("GUI系统就绪 - 按右Shift打开");
        LOGGER.info("HUD编辑器 - 按H键打开");
        LOGGER.info("已添加的HUD元素: 水印、FPS、坐标、模块列表、玩家状态、性能监控、敌人状态、游戏信息");
    }

    // ========== Getter 方法 ==========

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public KeybindManager getKeybindManager() {
        return this.keybindManager;
    }

    public HudManager getHudManager() {
        return this.hudManager;
    }

    public HUD getHudModule() {
        return this.hudModule;
    }

    public KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }

    /**
     * 获取模块统计信息
     */
    public String getModuleStats() {
        if (moduleManager == null) return "模块: 0/0";

        long enabledCount = moduleManager.getModules().stream()
                .filter(module -> module.isEnabled())
                .count();
        return String.format("模块: %d/%d", enabledCount, moduleManager.getModules().size());
    }

    /**
     * 获取HUD元素统计信息
     */
    public String getHudStats() {
        if (hudManager == null) return "HUD元素: 0";

        int visibleCount = 0;
        if (hudManager.getElements() != null) {
            visibleCount = (int) hudManager.getElements().stream()
                    .filter(element -> element.isVisible())
                    .count();
        }
        return String.format("HUD元素: %d/%d", visibleCount,
                hudManager.getElements() != null ? hudManager.getElements().size() : 0);
    }

    /**
     * 静态获取实例
     */
    public static TianyiClient getInstance() {
        return INSTANCE;
    }
}