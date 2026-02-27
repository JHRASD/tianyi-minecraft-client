package com.example.tianyiclient.mixin;

import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySpawnS2CPacket.class)
public abstract class EntitySpawnS2CPacketMixin {

    @Shadow private int entityId;
    @Shadow private double x;
    @Shadow private double y;
    @Shadow private double z;
    @Shadow private EntityType<?> entityType;

    public int getEntityId() {
        return this.entityId;
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}