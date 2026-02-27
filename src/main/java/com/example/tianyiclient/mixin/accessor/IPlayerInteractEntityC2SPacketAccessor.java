package com.example.tianyiclient.mixin.accessor;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface IPlayerInteractEntityC2SPacketAccessor {

    @Accessor("entityId")
    int getEntityId();

    // 目标类已经有 isPlayerSneaking()，不需要 Accessor

    @Accessor("type")
    Object getType();
}