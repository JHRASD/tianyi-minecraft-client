package com.example.tianyiclient.modules.misc;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.network.PacketEvent;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.c2s.play.*;

public class PacketLogger extends Module {

    public PacketLogger() {
        super("记录网络包", "记录网络包", Category.其他);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!isEnabled()) return;

        String direction = event.isSend() ? "§c[发送]§f" : "§a[接收]§f";
        String packetName = event.getPacket().getClass().getSimpleName();
        System.out.println(direction + " " + packetName);
    }
}