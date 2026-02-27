package com.example.tianyiclient.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract float getHealth();                 // method_6032
    @Shadow public abstract float getMaxHealth();              // method_6063
    @Shadow public abstract boolean isDead();                   // method_29504
    @Shadow public abstract ItemStack getMainHandStack();       // method_6047
    @Shadow public abstract ItemStack getOffHandStack();        // method_6079

    public float getHealthValue() { return getHealth(); }
    public float getMaxHealthValue() { return getMaxHealth(); }
    public boolean isDeadValue() { return isDead(); }

    // 先注释掉有问题的注入
    // @Inject(method = "method_64397(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
    //         at = @At("HEAD"), cancellable = true)
    // private void onDamage(World world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    //     // 可取消伤害
    // }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        // 跳跃事件
    }

    // @Inject(method = "method_6091(Lnet/minecraft/class_243;)V",
    //         at = @At("HEAD"))
    // private void onTravel(Vec3d movementInput, CallbackInfo ci) {
    //     // 移动事件
    // }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(double strength, double x, double z, CallbackInfo ci) {
        // 取消击退
    }
}