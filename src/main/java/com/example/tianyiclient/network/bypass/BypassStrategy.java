package com.example.tianyiclient.network.bypass;

import com.example.tianyiclient.network.ClientPacketIntent;

/**
 * 数据包绕过策略接口。
 * 所有具体的绕过策略都必须实现此接口。
 * 核心思想：将单个意图（Intent）分解、延迟、混淆成一系列看似自然的数据包序列。
 */
public interface BypassStrategy {

    /**
     * 策略类型枚举。
     */
    enum StrategyType {
        /**
         * 状态化绕过：将操作拆分为多个连续状态（如潜行、挥臂、取消潜行）。
         * 适用于攻击、交互等瞬时动作，用于对抗检测瞬时异常的模式。
         */
        STATEFUL,

        /**
         * 时序随机化绕过：在合法的时间窗口内随机化数据包发送的精确时刻。
         * 用于打乱过于精准的计时，模拟人类反应延迟。
         */
        TIMING_RANDOMIZATION,

        /**
         * 序列混淆绕过：在操作前后插入无关但合法的数据包（如轻微视角晃动、无用交互）。
         * 用于稀释操作的特征，使其隐藏在日常数据流中。
         */
        SEQUENCE_OBFUSCATION,

        /**
         * 被动兼容模式：几乎不修改，仅做最必要的封装。用于低风险环境或测试。
         */
        PASSTHROUGH
    }

    /**
     * 获取此策略的类型。
     * @return 策略类型
     */
    StrategyType getType();

    /**
     * 获取此策略的优先级（0-100）。数值越高，在条件符合时越可能被管理器选用。
     * 例如，在玩家潜行时，状态化策略的优先级应提高。
     * @return 策略优先级
     */
    int getPriority();

    /**
     * 评估当前游戏上下文，判断此策略是否适合被采用。
     * 这是策略是否被选中的关键判断。
     * @param context 当前的游戏上下文信息（后续由 BypassManager 提供）
     * @return 此策略在当前上下文下的适用性分数（0.0 - 1.0），越高越适用
     */
    float evaluateApplicability(BypassContext context);

    /**
     * 核心方法：执行绕过计划。
     * 根据给定的客户端意图，生成并执行一个安全的数据包序列。
     * @param intent 客户端原始意图（如“攻击实体A”）
     * @param context 当前的游戏上下文
     * @return 一个代表计划执行结果的令牌，可用于取消或查询状态（未来扩展）
     */
    ExecutionToken executePlan(ClientPacketIntent intent, BypassContext context);

    /**
     * 执行令牌（暂为标记接口，未来可扩展取消、查询进度等功能）。
     */
    interface ExecutionToken {
        // 目前作为占位符，标识一个正在执行或已执行的计划。
        // 例如：`token.cancel()` 可以用于在玩家突然移动时取消未完成的攻击序列。
    }
}