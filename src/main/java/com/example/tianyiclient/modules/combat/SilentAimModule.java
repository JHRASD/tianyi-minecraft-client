package com.example.tianyiclient.modules.combat;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.*;
import com.example.tianyiclient.utils.Rotation;
import com.example.tianyiclient.managers.PacketManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

/**
 * Silent Aim模块 - Grim服务器专用优化版
 */
public class SilentAimModule extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();

    // 基础设置
    private final BoolSetting targetPlayers;
    private final BoolSetting targetMobs;
    private final IntegerSetting range;
    private final IntegerSetting fov;
    private final BoolSetting requireLooking;
    private final BoolSetting debugMode;
    private final BoolSetting autoSendPackets;
    private final BoolSetting instantAttack;
    private final IntegerSetting attackRange;

    // Grim服务器专用设置
    private final BoolSetting grimCompatibility;
    private final BoolSetting useHumanizedRotation;
    private final IntegerSetting rotationSmoothing;
    private final IntegerSetting maxAnglePerTick;
    private final BoolSetting randomizeAttackTime;
    private final BoolSetting limitAttackFrequency;
    private final BoolSetting useSmartTargeting;
    private final IntegerSetting packetFrequency;
    private final IntegerSetting maxTargetsToCheck;
    private final BoolSetting addAntiCheatOffset;
    private final IntegerSetting offsetAmount;

    // 运行时变量
    private Entity currentTarget = null;
    private Rotation targetRotation = null;
    private boolean isRegistered = false;
    private int packetTimer = 0;
    private int grimAttackCooldown = 0;
    private int grimRotationDelay = 0;
    private float lastSentYaw = 0;
    private float lastSentPitch = 0;
    private long lastTargetLogTime = 0;
    private static final long LOG_INTERVAL = 1000;
    private int attackCooldown = 0;

    public SilentAimModule() {
        super("静默瞄准", "客户端视角不变，只修改发包角度", Category.战斗);

        // 基础设置
        targetPlayers = new BoolSetting("瞄准玩家", "瞄准其他玩家", true);
        targetMobs = new BoolSetting("瞄准怪物", "瞄准敌对生物", true);
        range = new IntegerSetting("范围", "瞄准范围（格）", 50, 10, 200);
        fov = new IntegerSetting("FOV限制", "视野内角度限制", 180, 30, 180);
        requireLooking = new BoolSetting("需要看向目标", "必须准星对准目标", false);
        debugMode = new BoolSetting("调试模式", "显示调试信息", false); // Grim服务器建议关闭
        autoSendPackets = new BoolSetting("自动发包", "自动发送旋转包", true);
        instantAttack = new BoolSetting("即时攻击", "立即攻击目标", true);
        attackRange = new IntegerSetting("攻击范围", "最大攻击距离", 4, 1, 10);

        // Grim服务器专用设置
        grimCompatibility = new BoolSetting("Grim兼容", "启用Grim反作弊专用优化", true);
        useHumanizedRotation = new BoolSetting("人性化旋转", "模拟人类旋转速度", true);
        rotationSmoothing = new IntegerSetting("旋转平滑度", "角度变化平滑度", 15, 1, 50);
        maxAnglePerTick = new IntegerSetting("最大角度/每tick", "每tick最大旋转角度", 20, 1, 45);
        randomizeAttackTime = new BoolSetting("随机化攻击时间", "随机攻击延迟", true);
        limitAttackFrequency = new BoolSetting("限制攻击频率", "防止过快攻击", true);
        useSmartTargeting = new BoolSetting("智能目标", "使用智能目标评分系统", true);
        packetFrequency = new IntegerSetting("发包频率", "每多少tick发送一次旋转包", 4, 1, 20);
        maxTargetsToCheck = new IntegerSetting("最大检查数", "每帧检查的最大实体数", 20, 5, 100);
        addAntiCheatOffset = new BoolSetting("防检测偏移", "添加微小随机偏移", true);
        offsetAmount = new IntegerSetting("偏移量", "随机偏移强度", 10, 1, 50);

        // 添加设置
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(range);
        addSetting(fov);
        addSetting(requireLooking);
        addSetting(debugMode);
        addSetting(autoSendPackets);
        addSetting(instantAttack);
        addSetting(attackRange);

        addSetting(grimCompatibility);
        addSetting(useHumanizedRotation);
        addSetting(rotationSmoothing);
        addSetting(maxAnglePerTick);
        addSetting(randomizeAttackTime);
        addSetting(limitAttackFrequency);
        addSetting(useSmartTargeting);
        addSetting(packetFrequency);
        addSetting(maxTargetsToCheck);
        addSetting(addAntiCheatOffset);
        addSetting(offsetAmount);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        setDisplayInfo("已启用");

        if (debugMode.getValue()) {
            System.out.println("[SilentAim] ✅ 模块启用");
        }

        registerEvents();

        PacketManager pm = PacketManager.getInstance();
        pm.setSilentAimEnabled(true);
        pm.setDebugMode(debugMode.getValue());

        // 启用Grim模式
        if (grimCompatibility.getValue()) {
            pm.setGrimMode(true);
        }

        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§a[SilentAim] 已启用"), false);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        setDisplayInfo(null);

        if (debugMode.getValue()) {
            System.out.println("[SilentAim] ❌ 模块禁用");
        }

        unregisterEvents();

        PacketManager pm = PacketManager.getInstance();
        pm.setSilentAimEnabled(false);
        pm.setSilentAimRotation(null);
        pm.setGrimMode(false);

        currentTarget = null;
        targetRotation = null;
        packetTimer = 0;
        attackCooldown = 0;
        grimAttackCooldown = 0;
        grimRotationDelay = 0;

        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§c[SilentAim] 已禁用"), false);
        }
    }

    private void registerEvents() {
        if (isRegistered) return;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isEnabled() && client.player != null) {
                onClientTick();
            }
        });

        isRegistered = true;
    }

    private void unregisterEvents() {
        isRegistered = false;
    }

    private void onClientTick() {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        int currentTick = mc.player.age;

        // Grim兼容性逻辑
        if (grimCompatibility.getValue()) {
            grimCompatibilityLogic();
        }

        // 降低频率：每n个tick执行一次完整逻辑
        int frequency = packetFrequency.getValue();
        if (currentTick % frequency != 0 && currentTarget == null) {
            packetTimer++;
            return;
        }

        // 攻击冷却计时
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (grimAttackCooldown > 0) {
            grimAttackCooldown--;
        }

        PacketManager pm = PacketManager.getInstance();
        if (!pm.isSilentAimEnabled()) {
            pm.setSilentAimEnabled(true);
        }

        pm.setDebugMode(debugMode.getValue());

        // 查找目标
        Entity target = findBestTarget();

        if (target != null) {
            // 计算角度
            targetRotation = calculateAngleTo(target);

            // 设置到PacketManager
            pm.setSilentAimRotation(targetRotation);
            currentTarget = target;

            // 更新显示信息
            setDisplayInfo("§a" + target.getName().getString() +
                    " §7(" + String.format("%.1f", mc.player.distanceTo(target)) + "m)");

            // 控制日志频率
            long currentTime = System.currentTimeMillis();
            if (debugMode.getValue() && currentTime - lastTargetLogTime > 2000) {
                System.out.println("[SilentAim] 🎯 锁定: " + target.getName().getString() +
                        " | 距离: " + String.format("%.1f", mc.player.distanceTo(target)) +
                        " | 角度: " + targetRotation);
                lastTargetLogTime = currentTime;
            }

            // Grim服务器专用攻击
            if (grimCompatibility.getValue() && instantAttack.getValue() &&
                    mc.options.attackKey.isPressed() && grimAttackCooldown <= 0 &&
                    mc.player.distanceTo(target) <= attackRange.getValue()) {

                attackEntityGrim(target);
            }
            // 普通服务器攻击
            else if (!grimCompatibility.getValue() && instantAttack.getValue() &&
                    mc.options.attackKey.isPressed() && attackCooldown <= 0 &&
                    mc.player.distanceTo(target) <= attackRange.getValue()) {

                attackEntityWithSilentAim(target);
            }
        } else {
            // 没有目标时，每2秒才清空一次角度
            if (currentTick % 40 == 0) {
                pm.setSilentAimRotation(null);
                currentTarget = null;
                setDisplayInfo("搜索中...");
            }
        }

        // 自动发包
        if (autoSendPackets.getValue() && targetRotation != null) {
            if (packetTimer >= frequency) {
                packetTimer = 0;
                sendRotationPacket();
            } else {
                packetTimer++;
            }
        }
    }

    private void grimCompatibilityLogic() {
        if (grimAttackCooldown > 0) {
            grimAttackCooldown--;
        }

        if (grimRotationDelay > 0) {
            grimRotationDelay--;
        }

        if (mc.player != null) {
            // 🔥 1.21.8版本修复：使用正确的字段名
            boolean isMoving = false;
            boolean isSprinting = false;

            try {
                // 获取玩家输入
                net.minecraft.client.input.Input input = mc.player.input;

                if (input != null) {
                    // 在1.21.8中，这些字段可能是：
                    // 1. movementForward -> pressingForward 或 forwardMovement
                    // 2. movementSideways -> pressingSideways 或 sidewaysMovement

                    // 尝试使用反射来获取所有可能的字段名
                    isMoving = checkMovementByReflection(input) || checkMovementByKeys();
                }

                isSprinting = mc.player.isSprinting();

            } catch (Exception e) {
                // 如果出现异常，使用按键检测作为备用
                isMoving = checkMovementByKeys();
                isSprinting = mc.player.isSprinting();
            }

            if (isMoving) {
                grimRotationDelay = Math.max(grimRotationDelay, 1);
            }

            if (isSprinting) {
                grimRotationDelay = Math.max(grimRotationDelay, 2);
            }
        }
    }

    // 🔥 方法1：通过反射检查移动（兼容各种版本）
    private boolean checkMovementByReflection(net.minecraft.client.input.Input input) {
        try {
            Class<?> inputClass = input.getClass();

            // 尝试1.21.8可能的字段名
            String[] possibleForwardFields = {
                    "movementForward",    // 旧版本
                    "pressingForward",    // 可能的新字段名
                    "forwardMovement",    // 可能的新字段名
                    "forward",            // 简化字段名
                    "movementInputForward" // 更完整
            };

            String[] possibleSidewaysFields = {
                    "movementSideways",   // 旧版本
                    "pressingSideways",   // 可能的新字段名
                    "sidewaysMovement",   // 可能的新字段名
                    "strafe",             // 简化字段名
                    "sideways",           // 简化字段名
                    "movementInputSideways" // 更完整
            };

            float forwardValue = 0f;
            float sidewaysValue = 0f;

            // 尝试获取向前移动值
            for (String fieldName : possibleForwardFields) {
                try {
                    java.lang.reflect.Field field = inputClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    forwardValue = field.getFloat(input);
                    break; // 找到就退出
                } catch (NoSuchFieldException e) {
                    continue; // 尝试下一个字段名
                }
            }

            // 尝试获取侧向移动值
            for (String fieldName : possibleSidewaysFields) {
                try {
                    java.lang.reflect.Field field = inputClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    sidewaysValue = field.getFloat(input);
                    break; // 找到就退出
                } catch (NoSuchFieldException e) {
                    continue; // 尝试下一个字段名
                }
            }

            // 检查是否有移动
            return Math.abs(forwardValue) > 0.01f || Math.abs(sidewaysValue) > 0.01f;

        } catch (Exception e) {
            // 反射失败，返回false
            return false;
        }
    }

    // 🔥 方法2：通过按键状态检查移动（最可靠）
    private boolean checkMovementByKeys() {
        if (mc.options == null) return false;

        // 检查WASD按键状态
        boolean forwardPressed = mc.options.forwardKey.isPressed();
        boolean backwardPressed = mc.options.backKey.isPressed();
        boolean leftPressed = mc.options.leftKey.isPressed();
        boolean rightPressed = mc.options.rightKey.isPressed();

        return forwardPressed || backwardPressed || leftPressed || rightPressed;
    }

    // 🔥 方法3：通过速度检查移动（物理层面）
    private boolean checkMovementByVelocity() {
        if (mc.player == null) return false;

        // 获取玩家速度
        net.minecraft.util.math.Vec3d velocity = mc.player.getVelocity();
        if (velocity == null) return false;

        // 计算水平速度（忽略Y轴）
        double horizontalSpeedSquared = velocity.x * velocity.x + velocity.z * velocity.z;

        // 如果水平速度大于阈值，则认为在移动
        return horizontalSpeedSquared > 0.001; // 0.001对应约0.032m/s
    }

    private void sendRotationPacket() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (targetRotation == null) return;

        try {
            // 在Grim服务器上，使用平滑的旋转
            Rotation rotationToSend = targetRotation;
            if (grimCompatibility.getValue() && useHumanizedRotation.getValue()) {
                rotationToSend = getSmoothedRotation(targetRotation);
            }

            // 添加防检测偏移
            if (addAntiCheatOffset.getValue()) {
                rotationToSend = addRandomOffset(rotationToSend);
            }

            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.LookAndOnGround(
                    rotationToSend.getYaw(),
                    rotationToSend.getPitch(),
                    mc.player.isOnGround(),
                    false
            );

            mc.getNetworkHandler().sendPacket(packet);

            if (debugMode.getValue()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTargetLogTime > LOG_INTERVAL) {
                    System.out.println("[SilentAim] 📤 发送旋转包: " + rotationToSend);
                }
            }
        } catch (Exception e) {
            if (debugMode.getValue()) {
                System.err.println("[SilentAim] ❌ 发送包失败: " + e.getMessage());
            }
        }
    }

    private Rotation getSmoothedRotation(Rotation target) {
        float currentYaw = lastSentYaw;
        float currentPitch = lastSentPitch;

        float yawDiff = target.getYaw() - currentYaw;
        float pitchDiff = target.getPitch() - currentPitch;

        // 标准化角度差
        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;

        // 限制最大角度变化
        float maxAngle = maxAnglePerTick.getValue();
        yawDiff = Math.max(-maxAngle, Math.min(maxAngle, yawDiff));
        pitchDiff = Math.max(-maxAngle, Math.min(maxAngle, pitchDiff));

        // 应用平滑
        float smoothFactor = rotationSmoothing.getValue() / 100.0f;
        yawDiff *= smoothFactor;
        pitchDiff *= smoothFactor;

        float newYaw = currentYaw + yawDiff;
        float newPitch = currentPitch + pitchDiff;

        lastSentYaw = newYaw;
        lastSentPitch = newPitch;

        return new Rotation(newYaw, newPitch);
    }

    private Rotation addRandomOffset(Rotation rotation) {
        if (rotation == null) return null;

        float offsetStrength = offsetAmount.getValue() / 100.0f;
        float yawOffset = (random.nextFloat() - 0.5f) * 2.0f * offsetStrength;
        float pitchOffset = (random.nextFloat() - 0.5f) * 1.0f * offsetStrength;

        return new Rotation(rotation.getYaw() + yawOffset, rotation.getPitch() + pitchOffset);
    }

    public void attackEntityWithSilentAim(Entity target) {
        if (mc.player == null || mc.interactionManager == null || target == null) return;

        try {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d targetPos = target.getBoundingBox().getCenter();
            Vec3d diff = targetPos.subtract(eyePos);
            double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

            float yaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
            float pitch = (float) Math.toDegrees(Math.atan2(-diff.y, horizontalDistance));

            yaw = normalizeAngle(yaw);
            pitch = Math.max(-90, Math.min(90, pitch));

            PacketManager pm = PacketManager.getInstance();
            Rotation attackRotation = new Rotation(yaw, pitch);

            pm.setSilentAimRotation(attackRotation);
            pm.prepareRotationForAttack(target);

            for (int i = 0; i < 2; i++) {
                PlayerMoveC2SPacket lookPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                        yaw,
                        pitch,
                        mc.player.isOnGround(),
                        false
                );
                mc.getNetworkHandler().sendPacket(lookPacket);
            }

            mc.interactionManager.attackEntity(mc.player, target);
            attackCooldown = 10;

            if (debugMode.getValue()) {
                System.out.println("[SilentAim] ⚔️ 攻击目标: " + target.getName().getString());
            }
        } catch (Exception e) {
            System.err.println("[SilentAim] ❌ 攻击失败: " + e.getMessage());
        }
    }

    private void attackEntityGrim(Entity target) {
        if (mc.player == null || mc.interactionManager == null || target == null) return;
        if (grimAttackCooldown > 0) return;

        try {
            if (grimRotationDelay <= 0) {
                preparePreAiming(target);
                grimRotationDelay = 1 + random.nextInt(2);
                return;
            }

            executeGrimAttack(target);

        } catch (Exception e) {
            System.err.println("[GrimAim] ❌ 攻击失败: " + e.getMessage());
        }
    }

    private void preparePreAiming(Entity target) {
        Vec3d targetPos = calculateGrimTargetPos(target);
        Rotation rotation = calculateGrimRotation(targetPos);

        PacketManager pm = PacketManager.getInstance();
        pm.setSilentAimRotation(rotation);

        if (debugMode.getValue()) {
            System.out.println("[GrimAim] 🔍 预瞄: " + target.getName().getString());
        }
    }

    private void executeGrimAttack(Entity target) {
        PacketManager pm = PacketManager.getInstance();
        Rotation currentRotation = pm.getSilentAimRotation();

        if (currentRotation == null) return;

        float yaw = currentRotation.getYaw() + (random.nextFloat() - 0.5f) * 1.0f;
        float pitch = currentRotation.getPitch() + (random.nextFloat() - 0.5f) * 0.5f;

        yaw = normalizeAngle(yaw);
        pitch = Math.max(-90, Math.min(90, pitch));

        for (int i = 0; i < 1 + random.nextInt(2); i++) {
            PlayerMoveC2SPacket lookPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                    yaw,
                    pitch,
                    mc.player.isOnGround(),
                    false
            );
            mc.getNetworkHandler().sendPacket(lookPacket);

            try {
                Thread.sleep(1 + random.nextInt(2));
            } catch (InterruptedException e) {}
        }

        int attackDelay = randomizeAttackTime.getValue() ? random.nextInt(2) : 0;

        if (attackDelay > 0) {
            try {
                Thread.sleep(attackDelay);
            } catch (InterruptedException e) {}
        }

        mc.interactionManager.attackEntity(mc.player, target);

        if (debugMode.getValue()) {
            System.out.println("[GrimAim] ⚔️ 攻击: " + target.getName().getString());
        }

        grimAttackCooldown = limitAttackFrequency.getValue() ?
                5 + random.nextInt(5) : 3;
    }

    private Vec3d calculateGrimTargetPos(Entity target) {
        Vec3d center = target.getBoundingBox().getCenter();

        double offsetX = (random.nextDouble() - 0.5) * 0.08;
        double offsetY = (random.nextDouble() - 0.5) * 0.04;
        double offsetZ = (random.nextDouble() - 0.5) * 0.08;

        if (target instanceof LivingEntity) {
            double heightOffset = 0;
            if (random.nextFloat() > 0.7) {
                heightOffset = -0.15;
            } else if (random.nextFloat() > 0.5) {
                heightOffset = 0.2;
            }
            offsetY += heightOffset;
        }

        return center.add(offsetX, offsetY, offsetZ);
    }

    private Rotation calculateGrimRotation(Vec3d targetPos) {
        if (mc.player == null) return new Rotation(0, 0);

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = targetPos.subtract(eyePos);
        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, horizontalDistance));

        if (useHumanizedRotation.getValue()) {
            float lastYaw = lastSentYaw;
            float lastPitch = lastSentPitch;

            float yawDiff = (float)(yaw - lastYaw);
            float pitchDiff = (float)(pitch - lastPitch);

            while (yawDiff > 180) yawDiff -= 360;
            while (yawDiff < -180) yawDiff += 360;

            float maxAngle = maxAnglePerTick.getValue();
            yawDiff = Math.max(-maxAngle, Math.min(maxAngle, yawDiff));
            pitchDiff = Math.max(-maxAngle, Math.min(maxAngle, pitchDiff));

            float smoothFactor = rotationSmoothing.getValue() / 100.0f;
            yawDiff *= smoothFactor;
            pitchDiff *= smoothFactor;

            yaw = lastYaw + yawDiff;
            pitch = lastPitch + pitchDiff;
        }

        lastSentYaw = (float) yaw;
        lastSentPitch = (float) pitch;

        return new Rotation((float) yaw, (float) pitch);
    }

    private Entity findBestTarget() {
        if (mc.player == null || mc.world == null) return null;

        Entity bestTarget = null;

        if (useSmartTargeting.getValue()) {
            bestTarget = findBestTargetSmart();
        } else {
            bestTarget = findBestTargetSimple();
        }

        return bestTarget;
    }

    private Entity findBestTargetSimple() {
        Entity bestTarget = null;
        double bestDistance = Double.MAX_VALUE;

        int maxChecks = maxTargetsToCheck.getValue();
        int checkedCount = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (checkedCount >= maxChecks) break;
            checkedCount++;

            if (isValidTarget(entity)) {
                double distance = mc.player.distanceTo(entity);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestTarget = entity;
                }
            }
        }

        return bestTarget;
    }

    private Entity findBestTargetSmart() {
        Entity bestTarget = null;
        double bestScore = -9999.0;

        int maxChecks = maxTargetsToCheck.getValue();
        int checkedCount = 0;

        Vec3d playerPos = mc.player.getEyePos();
        Vec3d playerLook = mc.player.getRotationVec(1.0f);

        for (Entity entity : mc.world.getEntities()) {
            if (checkedCount >= maxChecks) break;

            if (isValidTarget(entity)) {
                checkedCount++;

                double score = calculateTargetScore(entity, playerPos, playerLook);

                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = entity;
                }
            }
        }

        if (debugMode.getValue() && bestTarget != null) {
            System.out.println("[SilentAim] 🔍 最佳目标: " + bestTarget.getName().getString() +
                    " | 评分: " + String.format("%.2f", bestScore));
        }

        return bestTarget;
    }

    private double calculateTargetScore(Entity entity, Vec3d playerPos, Vec3d playerLook) {
        double score = 0.0;

        double distance = mc.player.distanceTo(entity);
        double distanceScore = Math.max(0, range.getValue() - distance) / range.getValue() * 100;
        score += distanceScore * 0.6;

        Vec3d toTarget = entity.getBoundingBox().getCenter().subtract(playerPos).normalize();
        double dot = playerLook.dotProduct(toTarget);
        double fovScore = (dot + 1) / 2 * 100;
        score += fovScore * 0.4;

        if (mc.player.getVelocity().lengthSquared() > 0.01) {
            Vec3d playerVelocity = mc.player.getVelocity().normalize();
            double velocityDot = playerVelocity.dotProduct(toTarget);
            if (velocityDot > 0.7) {
                score += 15;
            }
        }

        return score;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null) return false;
        if (entity == mc.player) return false;
        if (!entity.isAlive()) return false;
        if (entity.isSpectator()) return false;

        double distance = mc.player.distanceTo(entity);
        if (distance > range.getValue()) return false;
        if (distance > attackRange.getValue()) return false;

        boolean isPlayer = entity instanceof PlayerEntity;
        boolean isMonster = entity instanceof Monster;

        if (!isPlayer && !isMonster) return false;
        if (isPlayer && !targetPlayers.getValue()) return false;
        if (isMonster && !targetMobs.getValue()) return false;

        if (fov.getValue() < 180) {
            Rotation toTarget = calculateAngleTo(entity);
            Rotation current = new Rotation(mc.player.getYaw(), mc.player.getPitch());
            float angleDiff = Math.abs(toTarget.getYaw() - current.getYaw());
            if (angleDiff > fov.getValue() / 2) {
                return false;
            }
        }

        return true;
    }

    private Rotation calculateAngleTo(Entity entity) {
        if (mc.player == null || entity == null) return new Rotation(0, 0);

        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox();
        Vec3d targetPos = box.getCenter();
        Vec3d diff = targetPos.subtract(eyePos);
        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, horizontalDistance));

        return new Rotation((float) yaw, (float) pitch);
    }

    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle > 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    public void testAttack() {
        Entity target = findBestTarget();
        if (target != null) {
            if (grimCompatibility.getValue()) {
                attackEntityGrim(target);
            } else {
                attackEntityWithSilentAim(target);
            }

            if (mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                        "§a测试攻击:\n" +
                                "§7目标: §f" + target.getName().getString() + "\n" +
                                "§7距离: §f" + String.format("%.1f", mc.player.distanceTo(target)) + "m"
                ), false);
            }
        } else {
            if (mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal("§c❌ 未找到目标"), false);
            }
        }
    }

    @Override
    public void toggle() {
        super.toggle();

        if (!isEnabled() && isRegistered) {
            unregisterEvents();
        }
    }
}