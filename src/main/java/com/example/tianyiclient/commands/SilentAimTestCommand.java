package com.example.tianyiclient.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.example.tianyiclient.managers.PacketManager;
import com.example.tianyiclient.utils.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

public class SilentAimTestCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder =
                    net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
                            .literal("testaim")
                            .executes(context -> {
                                // 测试看向最近的实体
                                testLookAtNearest(context.getSource());
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("enable")
                                    .executes(context -> {
                                        PacketManager.getInstance().setSilentAimEnabled(true);
                                        context.getSource().sendFeedback(Text.literal("§aSilent Aim 已启用"));
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("disable")
                                    .executes(context -> {
                                        PacketManager.getInstance().setSilentAimEnabled(false);
                                        context.getSource().sendFeedback(Text.literal("§cSilent Aim 已禁用"));
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("angle")
                                    .executes(context -> {
                                        // 设置固定角度
                                        Rotation testRotation = new Rotation(45f, 0f);
                                        PacketManager.getInstance().setSilentAimRotation(testRotation);
                                        context.getSource().sendFeedback(
                                                Text.literal("§a设置测试角度: yaw=45, pitch=0")
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("debug")
                                    .executes(context -> {
                                        // 切换调试模式
                                        PacketManager.getInstance().setDebugMode(true);
                                        context.getSource().sendFeedback(
                                                Text.literal("§a调试模式已启用")
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("status")
                                    .executes(context -> {
                                        // 显示状态
                                        PacketManager pm = PacketManager.getInstance();
                                        String status = String.format(
                                                "§eSilent Aim状态:\n" +
                                                        "§7启用: %s\n" +
                                                        "§7角度: %s\n" +
                                                        "§7最近目标: %s",
                                                pm.isSilentAimEnabled() ? "§a是" : "§c否",
                                                pm.getSilentAimRotation() != null ?
                                                        String.format("yaw=%.1f, pitch=%.1f",
                                                                pm.getSilentAimRotation().getYaw(),
                                                                pm.getSilentAimRotation().getPitch()) : "§c无",
                                                getNearestEntityName()
                                        );
                                        context.getSource().sendFeedback(Text.literal(status));
                                        return Command.SINGLE_SUCCESS;
                                    }));

            // 注册命令
            dispatcher.register(builder);
        });

        System.out.println("[SilentAimTestCommand] 命令已注册: /testaim");
    }

    private static void testLookAtNearest(FabricClientCommandSource source) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            source.sendFeedback(Text.literal("§c玩家或世界为空"));
            return;
        }

        // 查找最近的实体
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !entity.isAlive()) continue;

            double distance = mc.player.distanceTo(entity);
            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }

        if (nearest != null) {
            // 🔥 修复：使用新的lookAt(Entity)方法
            Rotation rotation = Rotation.lookAt(nearest);
            PacketManager.getInstance().setSilentAimRotation(rotation);
            PacketManager.getInstance().setSilentAimEnabled(true);

            source.sendFeedback(
                    Text.literal("§a瞄准最近的实体: §e" + nearest.getName().getString() +
                            " §7距离: §e" + String.format("%.1f", nearestDistance))
            );
            source.sendFeedback(
                    Text.literal("§7计算角度: yaw=" + rotation.getYaw() +
                            ", pitch=" + rotation.getPitch())
            );
        } else {
            source.sendFeedback(Text.literal("§c未找到目标实体"));
        }
    }

    private static String getNearestEntityName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return "§cN/A";

        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !entity.isAlive()) continue;

            double distance = mc.player.distanceTo(entity);
            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }

        return nearest != null ?
                String.format("§e%s §7(%.1fm)", nearest.getName().getString(), nearestDistance) :
                "§c无";
    }
}