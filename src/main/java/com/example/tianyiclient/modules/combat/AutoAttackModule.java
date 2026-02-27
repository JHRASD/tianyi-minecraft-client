package com.example.tianyiclient.modules.combat;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.*;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.modifiers.RedirectAttackModifier;
import java.util.Arrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AutoAttackModule extends Module {
    private RedirectAttackModifier attackModifier;
    private int targetId = -1;
    private Entity currentTarget = null;
    private int attackCooldown = 0;

    // 设置项引用
    private DoubleSetting rangeSetting;
    private EnumSetting attackModeSetting;
    private BoolSetting prioritizePlayersSetting;
    private BoolSetting attackHostileMobsSetting;
    private BoolSetting attackPassiveMobsSetting;
    private BoolSetting attackNeutralMobsSetting;
    private BoolSetting autoSwitchSetting;
    private DoubleSetting switchDelaySetting;
    private BoolSetting showTargetInfoSetting;
    private BoolSetting debugSetting;
    private DoubleSetting attackSpeedSetting;
    private BoolSetting autoTriggerSetting;

    public AutoAttackModule() {
        super("AutoAttack", "自动攻击附近的实体", Category.战斗);
    }

    @Override
    protected void init() {
        // ... 保持现有的设置初始化代码不变 ...
        // 1. 攻击范围设置
        rangeSetting = addSetting(new DoubleSetting(
                "攻击范围",
                "自动攻击的检测范围",
                10.0, 1.0, 50.0
        ));

        // 2. 攻击模式设置
        attackModeSetting = addSetting(new EnumSetting(
                "攻击模式",
                "选择要攻击的目标类型",
                "所有生物",
                new String[]{"仅玩家", "仅敌对生物", "所有生物", "所有实体"}
        ));

        // 3. 攻击速度设置
        attackSpeedSetting = addSetting(new DoubleSetting(
                "攻击速度",
                "自动攻击的速度（次/秒）",
                4.0, 0.5, 20.0
        ));

        // 4. 自动触发攻击
        autoTriggerSetting = addSetting(new BoolSetting(
                "自动触发",
                "自动发送攻击包",
                true
        ));

        // 5. 优先攻击玩家
        prioritizePlayersSetting = addSetting(new BoolSetting(
                "优先玩家",
                "优先攻击玩家而非怪物",
                true
        ));

        // 6. 攻击敌对生物
        attackHostileMobsSetting = addSetting(new BoolSetting(
                "攻击敌对生物",
                "攻击僵尸、骷髅等敌对生物",
                true
        ));

        // 7. 攻击被动生物
        attackPassiveMobsSetting = addSetting(new BoolSetting(
                "攻击被动生物",
                "攻击牛、羊等被动生物",
                false
        ));

        // 8. 攻击中立生物
        attackNeutralMobsSetting = addSetting(new BoolSetting(
                "攻击中立生物",
                "攻击狼、熊猫等中立生物",
                false
        ));

        // 9. 自动切换目标
        autoSwitchSetting = addSetting(new BoolSetting(
                "自动切换",
                "当前目标死亡或远离时自动切换",
                true
        ));

        // 10. 切换延迟
        switchDelaySetting = addSetting(new DoubleSetting(
                "切换延迟",
                "切换目标的延迟时间",
                5.0, 0.0, 20.0
        ));

        // 11. 显示目标信息
        showTargetInfoSetting = addSetting(new BoolSetting(
                "显示信息",
                "在聊天栏显示目标信息",
                true
        ));

        // 12. 检查间隔
        addSetting(new DoubleSetting(
                "检查间隔",
                "检查目标的间隔时间",
                3.0, 1.0, 20.0
        ));

        // 13. 距离权重
        addSetting(new DoubleSetting(
                "距离权重",
                "距离对目标选择的影响程度",
                1.0, 0.1, 5.0
        ));

        // 14. 忽略盔甲架
        addSetting(new BoolSetting(
                "忽略盔甲架",
                "不攻击盔甲架",
                true
        ));

        // 15. 忽略创造模式
        addSetting(new BoolSetting(
                "忽略创造模式",
                "不攻击创造模式玩家",
                true
        ));

        // 16. 队友保护
        addSetting(new BoolSetting(
                "队友保护",
                "不攻击相同队伍的玩家",
                true
        ));

        // 17. 调试模式
        debugSetting = addSetting(new BoolSetting(
                "调试模式",
                "在控制台显示详细调试信息",
                true
        ));

        // 设置快捷键
        setKeybind(org.lwjgl.glfw.GLFW.GLFW_KEY_H);
    }

    @Override
    protected void onEnable() {
        attackModifier = null;
        targetId = -1;
        currentTarget = null;
        attackCooldown = 0;

        debugLog("模块已启用");
        debugLog("攻击模式: " + attackModeSetting.getValue());
        debugLog("攻击范围: " + rangeSetting.getValue() + " 格");
        debugLog("攻击速度: " + attackSpeedSetting.getValue() + " 次/秒");

        if (showTargetInfoSetting.getValue() && mc.player != null) {
            mc.player.sendMessage(Text.literal("§a[自动攻击] 已启用"), false);
            mc.player.sendMessage(Text.literal("§7模式: " + attackModeSetting.getValue()), false);
        }
    }

    @Override
    protected void onDisable() {
        if (attackModifier != null) {
            PacketEngine.getInstance().unregisterModifier("auto_attack");
            debugLog("修改器已注销: auto_attack");
            attackModifier = null;
        }
        targetId = -1;
        currentTarget = null;
        attackCooldown = 0;

        debugLog("模块已禁用");

        if (showTargetInfoSetting.getValue() && mc.player != null) {
            mc.player.sendMessage(Text.literal("§c[自动攻击] 已关闭"), false);
        }
    }

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!isEnabled() || mc.world == null || mc.player == null) {
            return;
        }

        // 更新攻击冷却
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // 检查间隔控制
        double checkInterval = getDoubleSettingValue("检查间隔");
        if (checkInterval > 1 && mc.player.age % (int)checkInterval != 0) {
            return;
        }

        // 检查是否需要切换目标
        boolean needNewTarget = shouldFindNewTarget();

        if (needNewTarget) {
            // 根据攻击模式更新设置
            updateAttackSettingsFromMode();

            // 寻找新目标
            findAndSetTarget();
        }

        // 如果启用了自动触发且目标有效，发送攻击包
        if (autoTriggerSetting.getValue() && currentTarget != null && attackCooldown == 0) {
            sendAttackPacket();
        }

        // 更新显示信息
        updateDisplayInfo();
    }

    /**
     * 发送攻击包 - 修正版本
     */
    private void sendAttackPacket() {
        if (currentTarget == null || mc.player == null || mc.getNetworkHandler() == null) {
            return;
        }

        // 计算攻击间隔（tick）
        double attacksPerSecond = attackSpeedSetting.getValue();
        int attackInterval = Math.max(1, (int)(20.0 / attacksPerSecond));
        attackCooldown = attackInterval;

        // 检查目标是否仍然有效
        Entity target = mc.world.getEntityById(targetId);
        if (target == null || !target.isAlive()) {
            debugLog("目标无效，跳过攻击");
            return;
        }

        // 检查距离
        double range = rangeSetting.getValue();
        double distance = Math.sqrt(target.squaredDistanceTo(mc.player));
        if (distance > range) {
            debugLog("目标超出范围 (" + String.format("%.1f", distance) + " > " + range + ")");
            return;
        }

        try {
            // 创建攻击包 - 使用兼容性方法
            net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket attackPacket =
                    createAttackPacketCompat(target);

            if (attackPacket == null) {
                debugLog("创建攻击包失败");
                return;
            }

            debugLog("发送攻击包 -> 目标: " + target.getName().getString() + " (ID: " + targetId + ")");

            // 使用 PacketEngine 修改并发送包
            if (attackModifier != null) {
                // 使用修改器修改包
                PacketEngine.getInstance().modifyAndSend(attackPacket, "auto_attack");
                debugLog("攻击包已通过修改器发送");
            } else {
                // 如果没有修改器，直接发送
                mc.getNetworkHandler().sendPacket(attackPacket);
                debugLog("攻击包直接发送");
            }

            // 发送挥臂动画
            sendSwingAnimation();

        } catch (Exception e) {
            debugLog("发送攻击包失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 兼容性方法创建攻击包
     */
    private net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket createAttackPacketCompat(Entity target) {
        try {
            debugLog("尝试创建攻击包，目标ID: " + target.getId());

            // 方法1：尝试使用最新API
            try {
                // 1.21.8可能使用不同的构造函数
                // 尝试查找正确的构造函数
                Constructor<?>[] constructors = net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.class.getDeclaredConstructors();

                for (Constructor<?> constructor : constructors) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    debugLog("找到构造函数参数: " + Arrays.toString(paramTypes));

                    // 寻找接受Entity和boolean的构造函数
                    if (paramTypes.length == 2 && paramTypes[0] == Entity.class && paramTypes[1] == boolean.class) {
                        constructor.setAccessible(true);
                        return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(target, false);
                    }

                    // 寻找接受Entity和Hand的构造函数（旧版本）
                    if (paramTypes.length == 2 && paramTypes[0] == Entity.class && paramTypes[1] == Hand.class) {
                        constructor.setAccessible(true);
                        return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(target, Hand.MAIN_HAND);
                    }

                    // 寻找接受int和某种交互类型的构造函数
                    if (paramTypes.length == 3 && paramTypes[0] == int.class && paramTypes[1] == boolean.class) {
                        constructor.setAccessible(true);
                        return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(target.getId(), false, getInteractType());
                    }
                }
            } catch (Exception e) {
                debugLog("方法1失败: " + e.getMessage());
            }

            // 方法2：使用反射创建包（更通用的方法）
            try {
                // 获取PlayerInteractEntityC2SPacket类
                Class<?> packetClass = net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.class;

                // 尝试不同的构造函数
                // 1.21.8可能使用: (int entityId, boolean playerSneaking, InteractTypeHandler type)
                try {
                    // 先获取InteractTypeHandler类型（如果有）
                    Class<?> interactTypeHandlerClass = null;
                    try {
                        interactTypeHandlerClass = Class.forName("net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractTypeHandler");
                    } catch (ClassNotFoundException e) {
                        // 可能不是这个类名
                    }

                    if (interactTypeHandlerClass != null) {
                        // 尝试获取攻击类型的InteractTypeHandler
                        Object attackInteractType = getAttackInteractType(interactTypeHandlerClass);
                        if (attackInteractType != null) {
                            Constructor<?> constructor = packetClass.getDeclaredConstructor(int.class, boolean.class, interactTypeHandlerClass);
                            constructor.setAccessible(true);
                            return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(
                                    target.getId(), false, attackInteractType
                            );
                        }
                    }
                } catch (Exception e) {
                    debugLog("方法2.1失败: " + e.getMessage());
                }

                // 尝试其他可能的构造函数
                // (int entityId, net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractionType type, boolean sneaking)
                try {
                    Class<?> interactionTypeClass = Class.forName("net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractionType");
                    Object attackType = null;

                    // 尝试获取ATTACK枚举值
                    for (Object enumConstant : interactionTypeClass.getEnumConstants()) {
                        if (enumConstant.toString().contains("ATTACK")) {
                            attackType = enumConstant;
                            break;
                        }
                    }

                    if (attackType != null) {
                        Constructor<?> constructor = packetClass.getDeclaredConstructor(int.class, interactionTypeClass, boolean.class);
                        constructor.setAccessible(true);
                        return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(
                                target.getId(), attackType, false
                        );
                    }
                } catch (Exception e) {
                    debugLog("方法2.2失败: " + e.getMessage());
                }

            } catch (Exception e) {
                debugLog("方法2失败: " + e.getMessage());
            }

            // 方法3：使用现有的攻击包并修改其目标ID
            // 这是最简单的方法，因为RedirectAttackModifier已经可以修改包
            debugLog("使用备用方案：创建虚拟包");

            // 创建一个攻击包（可能不完美，但会被修改器重定向）
            // 使用反射创建一个简单的包
            Constructor<?> constructor = net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.class.getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            return (net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) constructor.newInstance(target.getId());

        } catch (Exception e) {
            debugLog("所有创建方法都失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取攻击交互类型
     */
    private Object getAttackInteractType(Class<?> interactTypeHandlerClass) {
        try {
            // 尝试获取ATTACK字段
            for (java.lang.reflect.Field field : interactTypeHandlerClass.getDeclaredFields()) {
                if (field.getType() == interactTypeHandlerClass &&
                        (field.getName().equals("ATTACK") || field.getName().contains("ATTACK"))) {
                    field.setAccessible(true);
                    return field.get(null);
                }
            }

            // 如果是枚举，查找ATTACK值
            if (interactTypeHandlerClass.isEnum()) {
                for (Object enumConstant : interactTypeHandlerClass.getEnumConstants()) {
                    if (enumConstant.toString().contains("ATTACK")) {
                        return enumConstant;
                    }
                }
            }
        } catch (Exception e) {
            debugLog("获取攻击交互类型失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取交互类型（兼容性方法）
     */
    private Object getInteractType() {
        try {
            // 尝试查找InteractType类
            Class<?> interactTypeClass = Class.forName("net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType");
            if (interactTypeClass.isEnum()) {
                for (Object enumConstant : interactTypeClass.getEnumConstants()) {
                    if (enumConstant.toString().contains("ATTACK")) {
                        return enumConstant;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }

    /**
     * 发送挥臂动画
     */
    private void sendSwingAnimation() {
        if (mc.getNetworkHandler() == null) return;

        try {
            net.minecraft.network.packet.c2s.play.HandSwingC2SPacket swingPacket =
                    new net.minecraft.network.packet.c2s.play.HandSwingC2SPacket(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(swingPacket);
            debugLog("挥臂动画已发送");
        } catch (Exception e) {
            debugLog("发送挥臂动画失败: " + e.getMessage());
        }
    }

    // ... 保持其余所有方法不变（shouldFindNewTarget, findAndSetTarget, findBestTarget等）...
    /**
     * 判断是否需要寻找新目标
     */
    private boolean shouldFindNewTarget() {
        if (currentTarget == null) {
            debugLog("当前无目标，需要寻找新目标");
            return true;
        }

        // 检查目标是否还存在
        Entity target = mc.world.getEntityById(targetId);
        if (target == null) {
            debugLog("目标不存在 (ID: " + targetId + ")");
            return true;
        }

        if (!target.isAlive()) {
            debugLog("目标已死亡: " + target.getName().getString());
            return true;
        }

        // 检查目标是否超出范围
        double range = rangeSetting.getValue();
        double distance = Math.sqrt(target.squaredDistanceTo(mc.player));
        if (distance > range) {
            debugLog("目标超出范围: " + target.getName().getString() + " (" + String.format("%.1f", distance) + " > " + range + ")");
            return true;
        }

        // 如果启用了自动切换，检查是否有更好的目标
        if (autoSwitchSetting.getValue()) {
            Entity betterTarget = findBetterTarget(currentTarget);
            if (betterTarget != null && betterTarget != currentTarget) {
                debugLog("找到更好的目标: " + betterTarget.getName().getString());
                return true;
            }
        }

        return false;
    }

    /**
     * 寻找并设置攻击目标
     */
    private void findAndSetTarget() {
        Entity newTarget = findBestTarget();

        if (newTarget != null) {
            // 更新目标
            targetId = newTarget.getId();
            currentTarget = newTarget;

            debugLog("找到新目标: " + newTarget.getName().getString() + " (ID: " + targetId + ", 类型: " + getEntityTypeName(newTarget) + ")");

            // 更新修改器
            if (attackModifier != null) {
                PacketEngine.getInstance().unregisterModifier("auto_attack");
                debugLog("清理旧修改器");
            }

            attackModifier = new RedirectAttackModifier(targetId);
            PacketEngine.getInstance().registerModifier("auto_attack", attackModifier);

            debugLog("注册新修改器: auto_attack -> 目标ID: " + targetId);

            // 显示目标信息
            if (showTargetInfoSetting.getValue() && mc.player != null) {
                String targetType = getEntityTypeName(newTarget);
                mc.player.sendMessage(
                        Text.literal("§a[自动攻击] 目标: " + newTarget.getName().getString() + " §7(" + targetType + ")"),
                        false
                );
            }

        } else {
            // 没有找到目标
            if (currentTarget != null) {
                debugLog("目标丢失，清理状态");
                currentTarget = null;
                targetId = -1;

                if (attackModifier != null) {
                    PacketEngine.getInstance().unregisterModifier("auto_attack");
                    attackModifier = null;
                    debugLog("修改器已清理");
                }

                if (showTargetInfoSetting.getValue() && mc.player != null) {
                    mc.player.sendMessage(Text.literal("§c[自动攻击] 目标丢失"), false);
                }
            } else {
                debugLog("未找到任何有效目标");
            }
        }
    }

    /**
     * 根据攻击模式字符串更新设置
     */
    private void updateAttackSettingsFromMode() {
        String mode = attackModeSetting.getValue();
        debugLog("当前攻击模式: " + mode);

        switch (mode) {
            case "仅玩家":
                attackHostileMobsSetting.setValue(false);
                attackPassiveMobsSetting.setValue(false);
                attackNeutralMobsSetting.setValue(false);
                prioritizePlayersSetting.setValue(true);
                debugLog("模式设置: 仅玩家");
                break;
            case "仅敌对生物":
                attackHostileMobsSetting.setValue(true);
                attackPassiveMobsSetting.setValue(false);
                attackNeutralMobsSetting.setValue(false);
                prioritizePlayersSetting.setValue(false);
                debugLog("模式设置: 仅敌对生物");
                break;
            case "所有生物":
                attackHostileMobsSetting.setValue(true);
                attackPassiveMobsSetting.setValue(true);
                attackNeutralMobsSetting.setValue(true);
                prioritizePlayersSetting.setValue(false);
                debugLog("模式设置: 所有生物");
                break;
            case "所有实体":
                attackHostileMobsSetting.setValue(true);
                attackPassiveMobsSetting.setValue(true);
                attackNeutralMobsSetting.setValue(true);
                prioritizePlayersSetting.setValue(true);
                debugLog("模式设置: 所有实体");
                break;
        }
    }

    /**
     * 寻找最佳攻击目标
     */
    private Entity findBestTarget() {
        double range = rangeSetting.getValue();
        Box searchBox = mc.player.getBoundingBox().expand(range);
        List<Entity> allEntities = mc.world.getOtherEntities(mc.player, searchBox);

        debugLog("搜索范围内实体: " + allEntities.size() + " 个，范围: " + range + " 格");

        if (debugSetting.getValue()) {
            allEntities.forEach(e ->
                    debugLog("  实体: " + e.getName().getString() +
                            " (类型: " + e.getClass().getSimpleName() +
                            ", 距离: " + String.format("%.1f", Math.sqrt(e.squaredDistanceTo(mc.player))) + ")")
            );
        }

        // 过滤和评分
        List<ScoredEntity> scoredTargets = allEntities.stream()
                .filter(this::isValidTarget)
                .map(entity -> new ScoredEntity(entity, calculateScore(entity)))
                .filter(scored -> scored.score > 0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());

        debugLog("有效目标: " + scoredTargets.size() + " 个");

        if (scoredTargets.isEmpty()) {
            return null;
        }

        // 输出前3个目标信息
        for (int i = 0; i < Math.min(3, scoredTargets.size()); i++) {
            ScoredEntity scored = scoredTargets.get(i);
            debugLog("  目标" + (i+1) + ": " + scored.entity.getName().getString() +
                    " (分数: " + String.format("%.1f", scored.score) +
                    ", 类型: " + getEntityTypeName(scored.entity) + ")");
        }

        Entity bestTarget = scoredTargets.get(0).entity;
        debugLog("选择最佳目标: " + bestTarget.getName().getString() +
                " (分数: " + String.format("%.1f", scoredTargets.get(0).score) + ")");

        return bestTarget;
    }

    /**
     * 寻找更好的目标（用于自动切换）
     */
    private Entity findBetterTarget(Entity current) {
        double range = rangeSetting.getValue();
        Box searchBox = mc.player.getBoundingBox().expand(range);
        List<Entity> allEntities = mc.world.getOtherEntities(mc.player, searchBox);

        Optional<Entity> betterTarget = allEntities.stream()
                .filter(this::isValidTarget)
                .filter(entity -> entity != current)
                .max(Comparator.comparingDouble(this::calculateScore));

        if (betterTarget.isPresent() && betterTarget.get() != current) {
            double currentScore = calculateScore(current);
            double betterScore = calculateScore(betterTarget.get());

            if (betterScore > currentScore * 1.2) {
                debugLog("找到更好的目标: " + betterTarget.get().getName().getString() +
                        " (分数: " + String.format("%.1f", betterScore) +
                        " > 当前: " + String.format("%.1f", currentScore) + ")");
                return betterTarget.get();
            }
        }

        return null;
    }

    /**
     * 计算目标得分
     */
    private double calculateScore(Entity entity) {
        double score = 0;

        // 距离得分（越近得分越高）
        double distance = Math.sqrt(entity.squaredDistanceTo(mc.player));
        double range = rangeSetting.getValue();
        double distanceWeight = getDoubleSettingValue("距离权重");
        double distanceScore = (1.0 - Math.min(distance / range, 1.0)) * distanceWeight;
        score += distanceScore * 100;

        // 目标类型加分
        if (entity instanceof PlayerEntity) {
            score += 50;
        } else if (entity instanceof HostileEntity) {
            score += 30;
        } else if (entity instanceof MobEntity) {
            score += 10;
        }

        // 生命值低的优先
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            float healthPercent = living.getHealth() / living.getMaxHealth();
            score += (1.0 - healthPercent) * 20;
        }

        // 如果设置了优先玩家，且目标是玩家，额外加分
        if (prioritizePlayersSetting.getValue() && entity instanceof PlayerEntity) {
            score += 25;
        }

        return score;
    }

    /**
     * 检查是否是有效的攻击目标
     */
    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        LivingEntity living = (LivingEntity) entity;

        // 检查是否存活
        if (!living.isAlive()) {
            debugLog("实体死亡: " + entity.getName().getString());
            return false;
        }

        // 检查是否是玩家
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;

            // 不攻击自己
            if (player == mc.player) {
                return false;
            }

            // 检查创造模式
            if (getBoolSettingValue("忽略创造模式") && (player.isCreative() || player.isSpectator())) {
                debugLog("忽略创造模式玩家: " + player.getName().getString());
                return false;
            }

            debugLog("有效玩家目标: " + player.getName().getString());
            return true;
        }

        // 检查盔甲架
        if (getBoolSettingValue("忽略盔甲架") &&
                entity.getClass().getName().contains("ArmorStandEntity")) {
            debugLog("忽略盔甲架: " + entity.getName().getString());
            return false;
        }

        // 检查敌对生物
        if (entity instanceof HostileEntity) {
            boolean allowed = attackHostileMobsSetting.getValue();
            debugLog("敌对生物: " + entity.getName().getString() + " -> " + (allowed ? "允许" : "禁止"));
            return allowed;
        }

        // 检查被动生物
        if (entity instanceof PassiveEntity) {
            boolean allowed = attackPassiveMobsSetting.getValue();
            debugLog("被动生物: " + entity.getName().getString() + " -> " + (allowed ? "允许" : "禁止"));
            return allowed;
        }

        // 检查其他生物（MobEntity）
        if (entity instanceof MobEntity) {
            boolean allowed = attackNeutralMobsSetting.getValue();
            debugLog("中立生物: " + entity.getName().getString() + " -> " + (allowed ? "允许" : "禁止"));
            return allowed;
        }

        debugLog("无效目标类型: " + entity.getClass().getSimpleName());
        return false;
    }

    /**
     * 获取实体类型名称
     */
    private String getEntityTypeName(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return "玩家";
        } else if (entity instanceof HostileEntity) {
            return "敌对生物";
        } else if (entity instanceof PassiveEntity) {
            return "被动生物";
        } else if (entity instanceof MobEntity) {
            return "生物";
        } else {
            return "实体";
        }
    }

    /**
     * 更新显示信息
     */
    private void updateDisplayInfo() {
        if (currentTarget != null) {
            String targetName = currentTarget.getName().getString();
            if (targetName.length() > 8) {
                targetName = targetName.substring(0, 8) + "...";
            }
            String targetType = getEntityTypeName(currentTarget).substring(0, 1);
            setDisplayInfo("§a" + targetName + " §7" + targetType);
        } else {
            setDisplayInfo("§7无目标");
        }
    }

    @Override
    public String getInfo() {
        if (currentTarget != null) {
            return getEntityTypeName(currentTarget);
        }
        return "";
    }

    /**
     * 调试日志
     */
    private void debugLog(String message) {
        if (debugSetting.getValue()) {
            System.out.println("[AutoAttack] " + message);
        }
    }

    // ========== 辅助类 ==========

    private static class ScoredEntity {
        final Entity entity;
        final double score;

        ScoredEntity(Entity entity, double score) {
            this.entity = entity;
            this.score = score;
        }
    }
}