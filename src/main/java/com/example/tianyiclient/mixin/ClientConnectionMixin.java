package com.example.tianyiclient.mixin;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.events.network.PacketReceiveEvent;
import com.example.tianyiclient.event.events.network.PacketSendEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "channelRead0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
                    shift = At.Shift.BEFORE),
            cancellable = true)
    private void onHandlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketReceiveEvent event = new PacketReceiveEvent(packet);
        EventBus.getInstance().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"),
            method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            cancellable = true)
    private void onSendPacketHead(Packet<?> packet, io.netty.channel.ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        EventBus.getInstance().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}