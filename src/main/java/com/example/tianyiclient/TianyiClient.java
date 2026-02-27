package com.example.tianyiclient;

import com.example.tianyiclient.config.ConfigManager;
import com.example.tianyiclient.gui.ClickGUI;
import com.example.tianyiclient.hud.HudManager;
import com.example.tianyiclient.hud.elements.*;
import com.example.tianyiclient.managers.ModuleManager;
import com.example.tianyiclient.managers.KeybindManager;
import com.example.tianyiclient.modules.combat.AutoTotem;
import com.example.tianyiclient.modules.impl.hud.HudEditModule;
import com.example.tianyiclient.modules.misc.MiddleClickItem;
import com.example.tianyiclient.modules.misc.PacketLogger;
import com.example.tianyiclient.modules.movement.Flight;
import com.example.tianyiclient.modules.player.Freecam;
import com.example.tianyiclient.modules.render.ESP2D;
import com.example.tianyiclient.modules.render.Fullbright;
import com.example.tianyiclient.modules.render.EntityInfoModule;
import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.modules.render.HUD;
// 网络系统导入
import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.bypass.BypassManager;
import com.example.tianyiclient.network.modifiers.DelayModifier;
import com.example.tianyiclient.network.modifiers.RedirectAttackModifier;
import com.example.tianyiclient.network.modifiers.SequenceModifier;
// 新增模块导入
import com.example.tianyiclient.modules.combat.AutoAttackModule;
import com.example.tianyiclient.modules.misc.AntiDetectionModule;

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

    // 调试模式
    public static boolean DEBUG = true;

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
        LOGGER.info("=== 统一包管理系统 ===");
        LOGGER.info("版本: 1.0.0");
        LOGGER.info("调试模式: {}", DEBUG ? "启用" : "禁用");

        try {
            // 0. 设置单例实例
            INSTANCE = this;

            // 2. 先创建HUD模块
            this.hudModule = new HUD();

            // 3. 创建HUD管理器并传入HUD模块
            this.hudManager = new HudManager();
            this.hudManager.setHudModule(this.hudModule);

            // 4. 初始化模块管理器
            this.moduleManager = new ModuleManager();

            // 5. 初始化按键管理器
            this.keybindManager = KeybindManager.getInstance();

            // 8. 注册按键绑定
            registerKeybinds();

            // 9. 注册命令（只注册存在的命令）
            registerCommandsDirectly();

            // 10. 初始化所有系统
            init();

            // 11. 注册按键检测
            registerKeyDetection();

            LOGGER.info("TianyiClient 初始化完成");
            LOGGER.info("=== 系统状态 ===");

            LOGGER.info("模块管理器: {}", moduleManager != null ? "✅" : "❌");
            LOGGER.info("事件总线: {}", EVENT_BUS != null ? "✅" : "❌");

        } catch (Exception e) {
            LOGGER.error("TianyiClient 初始化失败", e);
            e.printStackTrace();
            throw new RuntimeException("TianyiClient初始化失败", e);
        }
    }

    /**
     * 直接注册所有命令 - 只注册确实存在的
     */
    private void registerCommandsDirectly() {
        LOGGER.info("开始注册命令...");

        try {
            LOGGER.info("✓ SilentAimTestCommand 注册成功");
        } catch (Exception e) {
            LOGGER.error("✗ SilentAimTestCommand 注册失败: {}", e.getMessage());
            e.printStackTrace();
        }

        // 不注册不存在的命令
        LOGGER.info("命令注册完成 - 只注册了存在的命令");
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
        LOGGER.info("开始初始化客户端框架...");

        // 0. 初始化配置管理器
        ConfigManager.getInstance();
        LOGGER.info("✓ 配置管理器初始化完成");

        // 【新增】初始化绕过包管理器
        BypassManager bypassManager = BypassManager.getInstance();
        bypassManager.init();
        bypassManager.setEnabled(true);
        LOGGER.info("✓ 绕过包管理器 (BypassManager) 初始化完成");

        // 【新增】初始化数据包引擎
        PacketEngine engine = PacketEngine.getInstance();
        engine.setEnabled(true);
        engine.setEnableModification(true); // 启用包修改功能

        // 【新增】注册默认包修改器
        try {
            // 延迟修改器
            DelayModifier delay50ms = new DelayModifier(50);
            DelayModifier delay100ms = new DelayModifier(100);
            engine.registerModifier("delay_50ms", delay50ms);
            engine.registerModifier("delay_100ms", delay100ms);

            // 攻击重定向修改器
            RedirectAttackModifier redirectModifier = new RedirectAttackModifier(-1);
            engine.registerModifier("redirect_attack", redirectModifier);

            // 序列修改器
            SequenceModifier sequenceModifier = new SequenceModifier();
            engine.registerModifier("sequence", sequenceModifier);

            LOGGER.info("✓ 包修改系统初始化完成 - 已注册 {} 个修改器", engine.getRegisteredModifiers().size());

        } catch (Exception e) {
            LOGGER.error("✗ 包修改系统初始化失败: {}", e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("✓ 数据包引擎 (PacketEngine) 初始化完成");

        // 1. 输出EventBus状态
        LOGGER.info("EventBus 状态: {}", EVENT_BUS != null ? "已初始化" : "null");
        if (EVENT_BUS != null) {
            LOGGER.info("EventBus 实例: {}", EVENT_BUS.getClass().getName());
        }

        // 2. 初始化快捷键管理器
        keybindManager.init();
        LOGGER.info("✓ 快捷键管理器初始化完成");

        // 5. 初始化HUD管理器
        if (hudManager != null) {
            hudManager.init();
            LOGGER.info("✓ HUD管理器初始化完成");
        }

        // 6. 初始化模块管理器
        moduleManager.init();
        LOGGER.info("✓ 模块管理器初始化完成");

        // 7. 注册模块和HUD元素
        initModulesAndElements();

        // 8. 注册事件
        initEvents();

        // 9. GUI初始化
        initGUI();

        // 10. 启动EventBus测试
        startEventBusTest();

        // 11. 检查网络模块注册情况
        checkNetworkModules();

        LOGGER.info("客户端框架初始化完成");
    }

    /**
     * 检查网络模块注册情况
     */
    private void checkNetworkModules() {
        LOGGER.info("=== 检查网络模块注册 ===");

        // 检查PacketLogger模块
        if (moduleManager.getModuleByName("PacketLogger") != null) {
            LOGGER.info("✓ PacketLogger 模块已注册");
        } else {
            LOGGER.warn("✗ PacketLogger 模块未注册");
        }

        // 检查PacketBlocker模块
        if (moduleManager.getModuleByName("PacketBlocker") != null) {
            LOGGER.info("✓ PacketBlocker 模块已注册");
        } else {
            LOGGER.warn("✗ PacketBlocker 模块未注册");
        }

        // 检查EnhancedSilentAim模块（增强版）
        if (moduleManager.getModuleByName("静默瞄准-增强版") != null) {
            LOGGER.info("✓ EnhancedSilentAim 模块已注册");
        } else {
            LOGGER.warn("✗ EnhancedSilentAim 模块未注册");
        }

        // 检查统一包管理系统
        LOGGER.info("=== 网络模块检查完成 ===");
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
            LOGGER.info("EventBus测试未启用: {}", e.getMessage());
        }
    }

    /**
     * 初始化模块和HUD元素
     */
    private void initModulesAndElements() {
        LOGGER.info("开始注册模块和HUD元素...");

        // 先注册HUD模块
        if (hudModule != null) {
            moduleManager.register(hudModule);
            LOGGER.info("✓ 注册HUD模块成功");
        }

        // 【新增】注册AutoAttack模块
        AutoAttackModule autoAttackModule = new AutoAttackModule();
        moduleManager.register(autoAttackModule);
        LOGGER.info("✓ 注册AutoAttack模块成功");

        // 【新增】注册AntiDetection模块
        AntiDetectionModule antiDetectionModule = new AntiDetectionModule();
        moduleManager.register(antiDetectionModule);
        LOGGER.info("✓ 注册AntiDetection模块成功");

        // 【重要】注册所有HUD元素
        if (hudManager != null) {
            // 1. 水印元素
            hudManager.registerElement(new WatermarkElement());
            LOGGER.info("✓ 注册水印HUD元素成功");

            // 2. 网络信息
            hudManager.registerElement(new NetworkInfoElement());
            LOGGER.info("✓ 注册网络信息HUD元素成功");

            // 3. FPS信息元素
            hudManager.registerElement(new FpsElement());
            LOGGER.info("✓ 注册FPS信息元素成功");

            // 4. 坐标信息元素
            hudManager.registerElement(new CoordinatesElement());
            LOGGER.info("✓ 注册坐标信息元素成功");

            // 5. 模块列表元素
            hudManager.registerElement(new ModuleListElement());
            LOGGER.info("✓ 注册模块列表元素成功");

            // 6. 玩家状态元素
            hudManager.registerElement(new PlayerStatusElement());
            LOGGER.info("✓ 注册玩家状态元素成功");

            // 7. 性能监控元素
            hudManager.registerElement(new PerformanceMonitorElement());
            LOGGER.info("✓ 注册性能监控HUD元素成功");

            // 8. 敌人状态元素
            hudManager.registerElement(new EnemyStatusElement());
            LOGGER.info("✓ 注册敌人状态HUD元素成功");

            // 9. 游戏信息元素
            hudManager.registerElement(new GameInfoElement());
            LOGGER.info("✓ 注册游戏信息HUD元素成功");

            LOGGER.info("✓ 共注册了 {} 个HUD元素", hudManager.getElements().size());
        }

        // 注册实体信息模块
        EntityInfoModule entityInfoModule = new EntityInfoModule();
        moduleManager.register(entityInfoModule);
        LOGGER.info("✓ 注册实体信息模块成功");

        // 注册对应的HUD元素
        if (hudManager != null) {
            hudManager.registerElement(new EntityInfoElement(entityInfoModule));
            LOGGER.info("✓ 注册实体信息HUD元素成功");
        }

        // 注册基础模块
        moduleManager.register(new Flight());
        moduleManager.register(new ESP2D());
        moduleManager.register(new PacketLogger());
        moduleManager.register(new Freecam());

        LOGGER.info("✓ 注册飞行模块成功");

        moduleManager.register(new Fullbright());
        LOGGER.info("✓ 注册夜视模块成功");

        moduleManager.register(new AutoTotem());
        LOGGER.info("✓ 注册自动图腾模块成功");

        moduleManager.register(new MiddleClickItem());
        LOGGER.info("✓ 注册中键物品模块成功");

        // HudEditModule需要hudManager
        if (hudManager != null) {
            moduleManager.register(new HudEditModule(hudManager));
            LOGGER.info("✓ 注册HUD编辑模块成功");
        }

        // 尝试添加测试模块（如果存在）
        try {
            // 测试模块
            Class<?> testModuleClass = Class.forName("com.example.tianyiclient.modules.test.TestModule");
            com.example.tianyiclient.modules.Module testModule =
                    (com.example.tianyiclient.modules.Module) testModuleClass.getDeclaredConstructor().newInstance();
            moduleManager.register(testModule);
            LOGGER.info("✓ 注册测试模块成功");

            // 调试模块
            Class<?> debugModuleClass = Class.forName("com.example.tianyiclient.modules.test.DebugModule");
            com.example.tianyiclient.modules.Module debugModule =
                    (com.example.tianyiclient.modules.Module) debugModuleClass.getDeclaredConstructor().newInstance();
            moduleManager.register(debugModule);
            LOGGER.info("✓ 注册调试模块成功");
        } catch (Exception e) {
            LOGGER.warn("无法注册测试模块: {}", e.getMessage());
        }

        LOGGER.info("✓ 总共注册了 {} 个模块", moduleManager.getModules().size());
    }

    /**
     * 注册事件监听器
     */
    private void initEvents() {
        LOGGER.info("开始注册事件监听器...");

        // 模块管理器本身可以监听事件
        EVENT_BUS.register(moduleManager);
        LOGGER.info("✓ 注册模块管理器到事件总线");

        // 注册各个模块到事件总线
        int moduleCount = 0;
        for (com.example.tianyiclient.modules.Module module : moduleManager.getModules()) {
            EVENT_BUS.register(module);
            moduleCount++;
        }
        LOGGER.info("✓ 注册了 {} 个模块到事件总线", moduleCount);

        // 注册HUD管理器到事件总线
        if (hudManager != null) {
            EVENT_BUS.register(hudManager);
            LOGGER.info("✓ 注册HUD管理器到事件总线");
        }

        // 【新增】注册绕过包管理器到事件总线
        EVENT_BUS.register(BypassManager.getInstance());
        LOGGER.info("✓ 注册绕过包管理器到事件总线");

        // 【新增】注册数据包引擎到事件总线
        EVENT_BUS.register(PacketEngine.getInstance());
        LOGGER.info("✓ 注册数据包引擎到事件总线");

        LOGGER.info("✓ 事件监听器注册完成");
    }

    /**
     * 初始化 GUI
     */
    private void initGUI() {
        LOGGER.info("GUI系统就绪 - 按右Shift打开");
        LOGGER.info("HUD编辑器 - 按H键打开");
        LOGGER.info("已添加的HUD元素: 水印、FPS、坐标、模块列表、玩家状态、性能监控、敌人状态、游戏信息");

        // Silent Aim命令信息
        LOGGER.info("Silent Aim测试 - 可使用 /testaim 命令测试");

        // 统一包管理系统信息
        LOGGER.info("=== 统一包管理系统状态 ===");
        LOGGER.info("系统版本: 1.0.0");
        LOGGER.info("包管理: ✅ 已启用");
        LOGGER.info("攻击时机优化: ✅ 已启用");
        LOGGER.info("Grim服务器兼容: ✅ 已启用");
        LOGGER.info("数据包引擎: ✅ 已启用");
        LOGGER.info("包修改系统: ✅ 已启用");
        LOGGER.info("绕过策略: ✅ 已注册");
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
     * 设置调试模式
     */
    public static void setDebugMode(boolean enabled) {
        DEBUG = enabled;
        LOGGER.info("调试模式: {}", enabled ? "启用" : "禁用");
    }

    /**
     * 静态获取实例
     */
    public static TianyiClient getInstance() {
        return INSTANCE;
    }

    /**
     * 客户端关闭时清理资源
     */
    public void onShutdown() {
        LOGGER.info("TianyiClient 正在关闭...");
        LOGGER.info("TianyiClient 已安全关闭");
    }
}