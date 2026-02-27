package com.example.tianyiclient.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract void addExperience(int experience);        // method_7255
    @Shadow public abstract void addExperienceLevels(int levels);      // method_7316
    @Shadow public abstract boolean isCreative();                       // method_68878

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        // 取消攻击
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // 修改交互结果
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        // 修改挖掘速度
    }

    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void onCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        // 强制可以吃东西
    }
}