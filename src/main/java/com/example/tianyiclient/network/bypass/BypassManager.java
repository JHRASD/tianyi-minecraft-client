package com.example.tianyiclient.network.bypass;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import net.minecraft.client.MinecraftClient;
import com.example.tianyiclient.network.ClientPacketIntent; // 修复 ClientPacketIntent 无法解析

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList; // 线程安全列表

/**
 * 绕过策略管理器。
 * 负责注册策略、维护当前游戏上下文，并根据上下文为数据包意图分配合适的绕过策略。
 * 这是一个单例类。
 */
public class BypassManager {

    // ---------- 单例实例 ----------
    private static final BypassManager INSTANCE = new BypassManager();

    // ---------- 核心组件 ----------
    private final List<BypassStrategy> registeredStrategies = new CopyOnWriteArrayList<>();
    private BypassContext currentContext = BypassContext.empty();
    private BypassStrategy forcedStrategy = null; // 可强制指定策略（用于调试或模块控制）

    // ---------- 状态与配置 ----------
    private boolean isEnabled = true;
    private long lastContextUpdateTick = 0;

    // 私有构造器
    private BypassManager() {
        // 私有化以防止外部构造
    }

    /**
     * 获取全局单例实例。
     */
    public static BypassManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化管理器。应在客户端加载完成后调用（例如在模组初始化时）。
     * 此方法会注册所有内置的绕过策略。
     */
    public void init() {
        System.out.println("[BypassManager] 初始化...");
        // 清空列表，防止重复初始化
        registeredStrategies.clear();

        // 注册具体策略实现类
        registerStrategy(new StatefulBypass()); // 这是我们刚创建的策略
        // 未来可以在这里添加更多策略：
        // registerStrategy(new TimingRandomizationBypass());
        // registerStrategy(new SequenceObfuscationBypass());

        System.out.println("[BypassManager] 初始化完成。当前注册策略数: " + registeredStrategies.size());
    }

    /**
     * 注册一个新的绕过策略。
     * @param strategy 要注册的策略
     */
    public void registerStrategy(BypassStrategy strategy) {
        if (strategy == null) {
            System.err.println("[BypassManager] 尝试注册一个空策略，已忽略。");
            return;
        }
        if (!registeredStrategies.contains(strategy)) {
            registeredStrategies.add(strategy);
            System.out.println("[BypassManager] 已注册策略: " + strategy.getType());
        }
    }

    /**
     * 取消注册一个绕过策略。
     * @param strategy 要取消注册的策略
     */
    public void unregisterStrategy(BypassStrategy strategy) {
        if (registeredStrategies.remove(strategy)) {
            System.out.println("[BypassManager] 已取消注册策略: " + (strategy != null ? strategy.getType() : "null"));
        }
    }

    /**
     * 事件总线监听方法：处理客户端刻更新。
     * 使用 @EventHandler 注解，事件总线会自动调用此方法。
     * @param event 游戏刻事件
     */
    @EventHandler(priority = com.example.tianyiclient.event.Priority.NORMAL)
    public void onTick(com.example.tianyiclient.event.events.client.TickEvent event) {
        this.onClientTick(event); // 调用原有的逻辑
    }

    /**
     * 每游戏刻更新。这是驱动管理器工作的核心循环。
     * 应由事件总线监听 TickEvent 并调用此方法。
     * @param tickEvent 游戏刻事件
     */
    public void onClientTick(TickEvent tickEvent) {
        if (!isEnabled || tickEvent.getPhase() != TickEvent.Phase.START) {
            return; // 仅在每刻开始时更新一次
        }

        // 更新当前游戏上下文快照
        updateContext(tickEvent);

        // 未来可以在这里添加周期性诊断或策略性能记录
    }

    /**
     * 更新当前游戏上下文。
     * @param tickEvent 游戏刻事件
     */
    private void updateContext(TickEvent tickEvent) {
        this.currentContext = BypassContext.snapshotCurrent(tickEvent);
        this.lastContextUpdateTick = tickEvent.getTickCount();
    }

    /**
     * 为给定的意图选择一个合适的绕过策略。
     * 这是管理器的核心决策逻辑。
     * @param intent 客户端数据包意图
     * @return 选中的策略，如果没有合适的策略则返回 null
     */
    public BypassStrategy selectStrategyForIntent(ClientPacketIntent intent) {
        if (!isEnabled) {
            System.out.println("[BypassManager] 管理器已禁用，无法选择策略。");
            return null;
        }

        // 1. 如果强制指定了策略，则直接返回
        if (forcedStrategy != null) {
            System.out.println("[BypassManager] 使用强制指定策略: " + forcedStrategy.getType());
            return forcedStrategy;
        }

        // 2. 如果没有可用策略，返回 null
        if (registeredStrategies.isEmpty()) {
            System.out.println("[BypassManager] 警告：没有注册任何绕过策略。");
            return null;
        }

        // 3. 根据上下文，为每个策略评分并选择最佳策略
        BypassStrategy bestStrategy = null;
        float bestScore = -1.0f;

        for (BypassStrategy strategy : registeredStrategies) {
            try {
                float score = strategy.evaluateApplicability(currentContext);
                // 只有得分大于0的策略才考虑
                if (score > 0 && score > bestScore) {
                    bestScore = score;
                    bestStrategy = strategy;
                }
            } catch (Exception e) {
                System.err.println("[BypassManager] 评估策略 " + strategy.getType() + " 时出错: " + e.getMessage());
            }
        }

        if (bestStrategy != null) {
            System.out.println("[BypassManager] 为意图 " + intent.getType() + " 选择策略: " +
                    bestStrategy.getType() + " (得分: " + String.format("%.2f", bestScore) + ")");
        } else {
            System.out.println("[BypassManager] 未找到适合意图 " + intent.getType() + " 的策略。");
        }

        return bestStrategy;
    }

    /**
     * 强制使用特定策略（用于调试或模块覆盖）。
     * @param strategy 要强制使用的策略，传入 null 则取消强制
     */
    public void setForcedStrategy(BypassStrategy strategy) {
        this.forcedStrategy = strategy;
        System.out.println("[BypassManager] 强制策略设置为: " + (strategy != null ? strategy.getType() : "无（自动选择）"));
    }

    // ========== Getter 方法 ==========

    public BypassContext getCurrentContext() {
        return currentContext;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        System.out.println("[BypassManager] " + (enabled ? "已启用" : "已禁用"));
    }

    public List<BypassStrategy> getRegisteredStrategies() {
        return new ArrayList<>(registeredStrategies); // 返回副本以保证安全
    }

    /**
     * 获取当前活跃的策略（如果是强制策略）或 null。
     */
    public BypassStrategy getActiveStrategy() {
        return forcedStrategy;
    }
}