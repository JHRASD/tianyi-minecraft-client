package com.example.tianyiclient.mixin.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntityAccessor {
    // 这些字段在 1.21.8 中不存在，所以移除
    // 取而代之，我们可以使用其他方式获取位置信息

    @Accessor("lastOnGround")
    boolean getLastOnGround();

    @Accessor("lastSprinting")
    boolean getLastSprinting();

    @Accessor("autoJumpEnabled")
    boolean isAutoJumpEnabled();

    @Invoker("sendMovementPackets")
    void invokeSendMovementPackets();

    @Invoker("pushOutOfBlocks")
    void invokePushOutOfBlocks(double x, double z);
}