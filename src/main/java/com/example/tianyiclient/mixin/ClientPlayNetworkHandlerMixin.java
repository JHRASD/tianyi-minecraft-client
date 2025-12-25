package com.example.tianyiclient.mixin;

// Mixin核心注解
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// Minecraft类
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
// 你的事件桥接类
import com.example.tianyiclient.event.events.network.PacketEventHelper;

/**
 * 这是一个针对 ClientPlayNetworkHandler 的Mixin类。
 * 用于在游戏发送和接收网络数据包时触发自定义事件。
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {


}