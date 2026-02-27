package com.example.tianyiclient.mixin;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import com.example.tianyiclient.mixin.accessor.IPlayerInteractEntityC2SPacketAccessor;

@Mixin(PlayerInteractEntityC2SPacket.class)
@Implements(@Interface(iface = IPlayerInteractEntityC2SPacketAccessor.class, prefix = "tianyiclient$"))
public abstract class PlayerInteractEntityC2SPacketMixin implements IPlayerInteractEntityC2SPacketAccessor {

    @Unique
    public boolean isAttack() {
        Object type = this.getType();
        if (type == null) return false;
        return type.getClass().getSimpleName().contains("Attack");
    }

    @Unique
    public boolean isInteract() {
        Object type = this.getType();
        if (type == null) return false;
        return type.getClass().getSimpleName().contains("Interact");
    }

    @Unique
    public boolean isInteractAt() {
        Object type = this.getType();
        if (type == null) return false;
        return type.getClass().getSimpleName().contains("InteractAt");
    }
}