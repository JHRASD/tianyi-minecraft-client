package com.example.tianyiclient.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Vec3d velocity;
    @Shadow private boolean velocityModified;
    @Shadow private boolean touchingWater;
    @Shadow private boolean inPowderSnow;
    @Shadow private boolean firstUpdate;

    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract void setVelocity(Vec3d velocity);
    @Shadow public abstract void addVelocity(double deltaX, double deltaY, double deltaZ);

    // 实例方法，供外部通过 cast 调用
    public Vec3d getEntityVelocity() {
        return getVelocity();
    }

    public void setEntityVelocity(Vec3d velocity) {
        setVelocity(velocity);
    }

    public void setVelocityModified(boolean modified) { this.velocityModified = modified; }
    public boolean isTouchingWater() { return this.touchingWater; }
    public boolean isInPowderSnow() { return this.inPowderSnow; }
    public boolean isFirstUpdate() { return this.firstUpdate; }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        // 移动事件
    }

    @Inject(method = "setPosition(DDD)V", at = @At("HEAD"))
    private void onSetPosition(double x, double y, double z, CallbackInfo ci) {
        // 位置改变事件
    }
}