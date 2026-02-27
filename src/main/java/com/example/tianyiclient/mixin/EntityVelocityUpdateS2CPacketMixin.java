package com.example.tianyiclient.mixin;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public abstract class EntityVelocityUpdateS2CPacketMixin {

    @Shadow private int entityId;
    @Shadow private int velocityX;
    @Shadow private int velocityY;
    @Shadow private int velocityZ;

    public int getEntityId() {
        return this.entityId;
    }

    public double getVelocityX() {
        return this.velocityX / 8000.0;
    }

    public double getVelocityY() {
        return this.velocityY / 8000.0;
    }

    public double getVelocityZ() {
        return this.velocityZ / 8000.0;
    }
}