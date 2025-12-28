package com.example.tianyiclient.modules.test;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.network.PacketEvent;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.BoolSetting;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

/**
 * 简单的包阻止测试模块
 * 可以阻止所有聊天消息
 */
public class PacketBlockerModule extends Module {

    private final BoolSetting blockChat;
    private final BoolSetting showBlocked;

    public PacketBlockerModule() {
        super("阻止特定类型的数据包", "阻止特定类型的数据包", Category.其他);

        blockChat = new BoolSetting("阻止聊天", "阻止所有聊天消息发送", false);
        showBlocked = new BoolSetting("显示阻止", "显示被阻止的包", true);

        addSetting(blockChat);
        addSetting(showBlocked);
    }

    @EventHandler(priority = Priority.HIGHEST)
    public void onPacketSend(PacketEvent event) {
        if (!isEnabled() || !(event instanceof com.example.tianyiclient.event.events.network.PacketSendEvent)) {
            return;
        }

        // 阻止聊天消息
        if (blockChat.isEnabled() && event.getPacket() instanceof ChatMessageC2SPacket) {
            event.setCancelled(true);

            if (showBlocked.isEnabled() && mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal("§c聊天消息已被阻止"), false);
            }
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        setDisplayInfo("已启用");
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        setDisplayInfo(null);
    }
}