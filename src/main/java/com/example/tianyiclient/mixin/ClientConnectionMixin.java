package com.example.tianyiclient.mixin;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.managers.PacketManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.Nullable;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    // ========== 核心Packet拦截方法 ==========
    /**
     * 拦截发送的数据包（在发送之前）
     * 签名对应：send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener)
     */
    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendPacketHead(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        PacketManager packetManager = PacketManager.getInstance();
        if (!packetManager.isEnabled()) return;

        if (!packetManager.handlePacketSend(packet)) {
            ci.cancel();
            TianyiClient.LOGGER.debug("[PacketSystem] 取消发送包: {}", packet.getClass().getSimpleName());
        }
    }

    /**
     * 拦截接收的数据包（在处理之前）
     * 注意：根据源码，我们注入的是channelRead0方法
     */
    @Inject(
            method = "channelRead0",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onChannelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketManager packetManager = PacketManager.getInstance();
        if (!packetManager.isEnabled()) return;

        if (!packetManager.handlePacketReceive(packet)) {
            ci.cancel();
            TianyiClient.LOGGER.debug("[PacketSystem] 取消接收包: {}", packet.getClass().getSimpleName());
        }
    }

    // ========== 其他网络事件处理方法 ==========
    /**
     * 发送包尾部（发送之后）
     */
    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("TAIL")
    )
    private void onSendPacketTail(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        TianyiClient.LOGGER.trace("[PacketSystem] 包已发送: {}", packet.getClass().getSimpleName());
    }

    /**
     * 网络连接事件 - 连接到状态查询服务器
     * 签名对应源码：connect(String address, int port, ClientQueryPacketListener listener)
     */
    @Inject(
            method = "connect(Ljava/lang/String;ILnet/minecraft/network/listener/ClientQueryPacketListener;)V",
            at = @At("HEAD")
    )
    private void onConnectStatus(String address, int port, net.minecraft.network.listener.ClientQueryPacketListener listener, CallbackInfo ci) {
        TianyiClient.LOGGER.info("[PacketSystem] 连接到状态服务器: {}:{}", address, port);
    }

    /**
     * 网络连接事件 - 连接到登录服务器
     * 签名对应源码：connect(String address, int port, ClientLoginPacketListener listener)
     */
    @Inject(
            method = "connect(Ljava/lang/String;ILnet/minecraft/network/listener/ClientLoginPacketListener;)V",
            at = @At("HEAD")
    )
    private void onConnectLogin(String address, int port, net.minecraft.network.listener.ClientLoginPacketListener listener, CallbackInfo ci) {
        TianyiClient.LOGGER.info("[PacketSystem] 连接到登录服务器: {}:{}", address, port);
    }

    /**
     * 静态连接方法 - 可选添加
     * 签名对应源码：public static ChannelFuture connect(InetSocketAddress address, boolean useEpoll, final ClientConnection connection)
     */
    @Inject(
            method = "connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;",
            at = @At("HEAD")
    )
    private static void onStaticConnect(InetSocketAddress address, boolean useEpoll, ClientConnection connection, CallbackInfoReturnable<io.netty.channel.ChannelFuture> cir) {
        TianyiClient.LOGGER.info("[PacketSystem] 静态连接方法调用: {}", address.toString());
    }

    /**
     * 网络断开事件
     */
    @Inject(
            method = "disconnect(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD")
    )
    private void disconnect(Text disconnectReason, CallbackInfo ci) {
        TianyiClient.LOGGER.info("[PacketSystem] 网络连接断开: {}", disconnectReason.getString());
    }

    /**
     * 网络异常事件
     */
    @Inject(
            method = "exceptionCaught",
            at = @At("HEAD")
    )
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        TianyiClient.LOGGER.warn("[PacketSystem] 网络异常: {}", throwable.getMessage());
    }
}