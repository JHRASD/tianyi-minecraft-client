package com.example.tianyiclient.commands;

import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.bypass.BypassManager;
import com.example.tianyiclient.network.modifiers.DelayModifier;
import com.example.tianyiclient.network.modifiers.RedirectAttackModifier;
import com.example.tianyiclient.network.modifiers.SequenceModifier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PacketTestCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return literal("packettest")
                .executes(context -> {
                    PacketEngine engine = PacketEngine.getInstance();
                    BypassManager bypass = BypassManager.getInstance();

                    context.getSource().sendMessage(Text.literal("§6=== 包系统测试 ==="));
                    context.getSource().sendMessage(Text.literal("§a状态: " + (engine.isEnabled() ? "§a启用" : "§c禁用")));
                    context.getSource().sendMessage(Text.literal("§a修改器: " + engine.getModifierCount() + " 个"));
                    context.getSource().sendMessage(Text.literal("§a绕过策略: " + bypass.getRegisteredStrategies().size() + " 个"));
                    context.getSource().sendMessage(Text.literal("§a输入 /packettest help 查看帮助"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("redirect")
                        .then(argument("entityId", IntegerArgumentType.integer(0, 10000))
                                .executes(context -> {
                                    int entityId = IntegerArgumentType.getInteger(context, "entityId");
                                    RedirectAttackModifier modifier = new RedirectAttackModifier(entityId);
                                    PacketEngine.getInstance().registerModifier("test_redirect", modifier);

                                    context.getSource().sendMessage(
                                            Text.literal("§a[测试] 重定向修改器已注册，目标ID: " + entityId)
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("delay")
                        .then(argument("ms", IntegerArgumentType.integer(0, 5000))
                                .executes(context -> {
                                    int delay = IntegerArgumentType.getInteger(context, "ms");
                                    DelayModifier modifier = new DelayModifier(delay);
                                    PacketEngine.getInstance().registerModifier("test_delay", modifier);

                                    context.getSource().sendMessage(
                                            Text.literal("§e[测试] 延迟修改器已注册: " + delay + "ms")
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("sequence")
                        .executes(context -> {
                            SequenceModifier modifier = new SequenceModifier()
                                    .setSequenceInterval(50);
                            PacketEngine.getInstance().registerModifier("test_sequence", modifier);

                            context.getSource().sendMessage(Text.literal("§b[测试] 序列修改器已注册"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("clear")
                        .executes(context -> {
                            PacketEngine.getInstance().clearAll();
                            context.getSource().sendMessage(Text.literal("§c[测试] 所有修改器已清除"));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("help")
                        .executes(context -> {
                            context.getSource().sendMessage(Text.literal("§6=== 包测试命令帮助 ==="));
                            context.getSource().sendMessage(Text.literal("§a/packettest §7- 查看状态"));
                            context.getSource().sendMessage(Text.literal("§a/packettest redirect <id> §7- 测试重定向"));
                            context.getSource().sendMessage(Text.literal("§a/packettest delay <ms> §7- 测试延迟"));
                            context.getSource().sendMessage(Text.literal("§a/packettest sequence §7- 测试序列"));
                            context.getSource().sendMessage(Text.literal("§a/packettest clear §7- 清除所有修改器"));
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}