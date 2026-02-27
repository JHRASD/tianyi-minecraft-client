package com.example.tianyiclient.modules.misc;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.network.PacketEngine;
import com.example.tianyiclient.network.bypass.BypassManager;
import com.example.tianyiclient.network.modifiers.DelayModifier;
import com.example.tianyiclient.network.modifiers.SequenceModifier;
import net.minecraft.text.Text;

public class AntiDetectionModule extends Module {
    private DelayModifier timingModifier;
    private SequenceModifier sequenceModifier;

    public AntiDetectionModule() {
        super("包修改", "使用包修改技术绕过反作弊检测", Category.其他);
    }

    @Override
    public void onEnable() {
        // 启用绕过管理器
        BypassManager.getInstance().setEnabled(true);

        // 注册时序干扰修改器（随机50-150ms延迟）
        timingModifier = new DelayModifier(50 + (int)(Math.random() * 100));
        PacketEngine.getInstance().registerModifier("anti_detect_delay", timingModifier);

        // 注册序列混淆修改器
        sequenceModifier = new SequenceModifier()
                .setSequenceInterval(30); // 30ms间隔

        PacketEngine.getInstance().registerModifier("anti_detect_sequence", sequenceModifier);

        // 启用包修改功能
        PacketEngine.getInstance().setEnableModification(true);

        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§a[反检测] 模块已启用"), false); // 添加第二个参数
        }
        System.out.println("✅ AntiDetection已启用");
    }

    @Override
    public void onDisable() {
        // 关闭绕过管理器
        BypassManager.getInstance().setEnabled(false);

        // 注销修改器
        PacketEngine.getInstance().unregisterModifier("anti_detect_delay");
        PacketEngine.getInstance().unregisterModifier("anti_detect_sequence");

        // 清理资源
        if (timingModifier != null) {
            timingModifier.shutdown();
            timingModifier = null;
        }

        if (sequenceModifier != null) {
            sequenceModifier.shutdown();
            sequenceModifier = null;
        }

        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§c[反检测] 模块已关闭"), false); // 添加第二个参数
        }
        System.out.println("❌ AntiDetection已关闭");
    }
}