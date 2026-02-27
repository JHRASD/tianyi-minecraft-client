package com.example.tianyiclient.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow private boolean lastSprinting;            // field_3919
    @Shadow private boolean lastOnGround;             // field_3920
    @Shadow private boolean autoJumpEnabled;          // field_3927

    // 辅助方法
    public boolean getLastSprinting() { return this.lastSprinting; }
    public boolean getLastOnGround() { return this.lastOnGround; }
    public boolean isAutoJumpEnabled() { return this.autoJumpEnabled; }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        // 取消发送移动包
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        // 取消方块推送
    }

    @Inject(method = "hasMovementInput", at = @At("RETURN"), cancellable = true)
    private void onHasMovementInput(CallbackInfoReturnable<Boolean> cir) {
        // 强制认为有移动输入：cir.setReturnValue(true);
    }

    @Inject(method = "shouldSlowDown", at = @At("RETURN"), cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> cir) {
        // 取消减速（灵魂沙等）：cir.setReturnValue(false);
    }
}