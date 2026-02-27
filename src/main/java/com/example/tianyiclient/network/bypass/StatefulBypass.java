package com.example.tianyiclient.network.bypass;

import com.example.tianyiclient.network.ClientPacketIntent;
import net.minecraft.client.MinecraftClient;
import com.example.tianyiclient.network.ClientPacketIntent; // 修复 ClientPacketIntent 无法解析
import net.minecraft.entity.Entity;                        // 修复 Entity 无法解析
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

/**
 * 状态化绕过策略。
 * 将瞬时操作（如攻击）分解为多个连续状态，并分散到多个游戏刻中执行，
 * 以此模拟真人操作的延迟，对抗基于时序检测的反作弊系统（如 Grim）。
 */
public class StatefulBypass implements BypassStrategy {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // 策略配置：每个状态持续的默认刻数（可根据风险动态调整）
    private int sneakPreparationTicks = 2;  // 攻击前预潜行刻数
    private int cooldownTicks = 1;          // 攻击后冷却刻数
    private final float baseApplicabilityScore = 0.7f; // 基础适用性分数

    @Override
    public StrategyType getType() {
        return StrategyType.STATEFUL;
    }

    @Override
    public int getPriority() {
        // 当玩家已经在潜行时，此策略优先级更高
        BypassContext currentContext = BypassManager.getInstance().getCurrentContext();
        if (currentContext != null && currentContext.isSneaking()) {
            return 85; // 高优先级
        }
        return 75; // 中等优先级
    }

    @Override
    public float evaluateApplicability(BypassContext context) {
        if (context.getPlayer() == null) {
            return 0.0f; // 无玩家，不适用
        }

        float score = baseApplicabilityScore;

        // 根据上下文微调分数
        // 1. 如果玩家正在移动，状态化操作更自然，略微加分
        if (context.getPlayerVelocity().lengthSquared() > 0.01) {
            score += 0.1f;
        }

        // 2. 如果服务器延迟高，状态化能更好地掩盖延迟，加分
        if (context.getServerPingMs() > 100) {
            score += 0.15f;
        }

        // 3. 如果玩家已经在潜行，此策略非常适用，大幅加分
        if (context.isSneaking()) {
            score += 0.2f;
        }

        // 4. 如果周围实体多（可能在战斗中），状态化更谨慎，加分
        if (context.getSurroundingEntityCount() > 3) {
            score += 0.1f;
        }

        // 确保分数在 0.0-1.0 之间
        return Math.max(0.0f, Math.min(1.0f, score));
    }

    @Override
    public ExecutionToken executePlan(ClientPacketIntent intent, BypassContext context) {
        System.out.println("[StatefulBypass] 开始执行状态化绕过计划，意图: " + intent);

        // 根据意图类型执行不同的状态化方案
        switch (intent.getType()) {
            case ATTACK_ENTITY:
                return executeStatefulAttack(intent, context);
            case INTERACT_ENTITY:
                // 未来实现：状态化交互
                System.out.println("[StatefulBypass] 状态化交互尚未实现，使用基础执行。");
                return executeBasic(intent, context);
            default:
                // 对于其他类型，暂时退化为直接执行
                System.out.println("[StatefulBypass] 意图类型 " + intent.getType() + " 未实现状态化，使用基础执行。");
                return executeBasic(intent, context);
        }
    }

    /**
     * 执行状态化攻击。
     * 典型流程：开始潜行（可选） -> 等待 -> 挥臂攻击 -> 等待 -> 停止潜行（如果之前开始了）
     */
    private ExecutionToken executeStatefulAttack(ClientPacketIntent intent, BypassContext context) {
        final Entity target = intent.getTargetEntity();
        if (target == null) {
            System.err.println("[StatefulBypass] 攻击意图的目标实体为 null");
            return null;
        }

        // 创建并返回一个令牌，它内部会安排异步执行
        return new StatefulAttackToken(target, intent, context);
    }

    /**
     * 基础执行（直接发送，无状态化）。用作回退方案。
     */
    private ExecutionToken executeBasic(ClientPacketIntent intent, BypassContext context) {
        // 这里暂时只是打印，后续会与 PacketEngine 联动
        System.out.println("[StatefulBypass] 基础执行: " + intent);
        // TODO: 调用 PacketEngine 发送原始数据包
        return new ExecutionToken() {}; // 返回一个匿名令牌
    }

    // ========== 内部令牌类：管理状态化攻击的执行过程 ==========

    /**
     * 状态化攻击执行令牌。
     * 内部维护一个状态机，并在每个游戏刻推进。
     */
    private class StatefulAttackToken implements ExecutionToken, Runnable {
        private final Entity target;
        private final ClientPacketIntent originalIntent;
        private final BypassContext startingContext;

        private int currentStep = 0;
        private long startTick;
        private boolean isCancelled = false;
        private boolean isCompleted = false;

        // 状态定义
        private static final int STEP_IDLE = 0;
        private static final int STEP_START_SNEAK = 1;
        private static final int STEP_WAIT_AFTER_SNEAK = 2;
        private static final int STEP_SWING_ATTACK = 3;
        private static final int STEP_WAIT_AFTER_ATTACK = 4;
        private static final int STEP_STOP_SNEAK = 5;
        private static final int STEP_DONE = 6;

        StatefulAttackToken(Entity target, ClientPacketIntent intent, BypassContext context) {
            this.target = target;
            this.originalIntent = intent;
            this.startingContext = context;
            this.startTick = context.getCurrentGameTick();

            System.out.println("[StatefulAttackToken] 创建，计划开始于刻: " + startTick);
            // 立即安排第一步执行（这里简化处理，实际应由 PacketEngine 调度）
            run();
        }

        @Override
        public void run() {
            if (isCancelled || isCompleted) {
                return;
            }

            BypassContext currentContext = BypassManager.getInstance().getCurrentContext();
            long currentTick = currentContext.getCurrentGameTick();

            // 检查是否超时
            if (currentTick > startTick + 20) { // 最多等待20刻（1秒）
                System.out.println("[StatefulAttackToken] 执行超时，取消。");
                isCancelled = true;
                return;
            }

            // 状态机执行
            switch (currentStep) {
                case STEP_IDLE:
                    // 决定是否需要先开始潜行
                    if (!startingContext.isSneaking()) {
                        currentStep = STEP_START_SNEAK;
                        System.out.println("[StatefulAttackToken] 步骤 1: 开始潜行");
                        // TODO: 发送开始潜行包
                        // sendStartSneakPacket();
                    } else {
                        currentStep = STEP_WAIT_AFTER_SNEAK; // 已经在潜行，跳过
                    }
                    break;

                case STEP_START_SNEAK:
                    // 等待潜行状态稳定（若干刻）
                    if (currentTick >= startTick + sneakPreparationTicks) {
                        currentStep = STEP_WAIT_AFTER_SNEAK;
                        System.out.println("[StatefulAttackToken] 潜行准备完成。");
                    }
                    break;

                case STEP_WAIT_AFTER_SNEAK:
                    // 短暂等待，模拟瞄准时间
                    if (currentTick >= startTick + sneakPreparationTicks + 1) {
                        currentStep = STEP_SWING_ATTACK;
                        System.out.println("[StatefulAttackToken] 步骤 2: 挥臂攻击实体 " + target.getName().getString());
                        // TODO: 发送攻击包
                        // sendAttackPacket(target);
                    }
                    break;

                case STEP_SWING_ATTACK:
                    // 攻击后冷却
                    if (currentTick >= startTick + sneakPreparationTicks + 1 + cooldownTicks) {
                        currentStep = STEP_WAIT_AFTER_ATTACK;
                        System.out.println("[StatefulAttackToken] 攻击完成，进入冷却。");
                    }
                    break;

                case STEP_WAIT_AFTER_ATTACK:
                    // 决定是否需要停止潜行
                    if (!startingContext.isSneaking()) {
                        currentStep = STEP_STOP_SNEAK;
                        System.out.println("[StatefulAttackToken] 步骤 3: 停止潜行");
                        // TODO: 发送停止潜行包
                        // sendStopSneakPacket();
                    } else {
                        currentStep = STEP_DONE; // 本来就该潜行，直接完成
                    }
                    break;

                case STEP_STOP_SNEAK:
                    // 完成
                    currentStep = STEP_DONE;
                    break;

                case STEP_DONE:
                    isCompleted = true;
                    System.out.println("[StatefulAttackToken] 状态化攻击流程全部完成。");
                    return;
            }

            // 安排下一刻继续执行（这里简化，实际应由引擎调度）
            // TODO: 需要由 PacketEngine 在下一刻再次调用此 run() 方法
        }

        // 可以添加取消方法
        public void cancel() {
            isCancelled = true;
            System.out.println("[StatefulAttackToken] 已被取消。");
        }
    }
}