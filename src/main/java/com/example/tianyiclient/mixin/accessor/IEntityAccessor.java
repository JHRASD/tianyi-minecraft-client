package com.example.tianyiclient.mixin.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface IEntityAccessor {
    @Accessor("velocityModified")
    void setVelocityModified(boolean modified);

    @Accessor("touchingWater")
    boolean isTouchingWater();

    @Accessor("inPowderSnow")
    boolean isInPowderSnow();

    @Accessor("firstUpdate")
    boolean isFirstUpdate();
}