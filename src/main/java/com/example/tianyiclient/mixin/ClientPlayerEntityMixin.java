package com.example.tianyiclient.mixin;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.events.player.MoveEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Unique
    private Vec3d tianyi$previousPosition;

    /**
     * 注入到 ClientPlayerEntity 的 tick() 方法开头。
     * tick() 方法是肯定存在的，可以确保Mixin能正常注入，验证事件系统连通性。
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        this.tianyi$previousPosition = player.getPos();
    }

    /**
     * 注入到 ClientPlayerEntity 的 tick() 方法末尾。
     * 比较移动前后的位置，如果发生变化则触发 MoveEvent。
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
        Vec3d currentPosition = player.getPos();

        // 安全判断：如果记录过之前的位置且位置发生了变化
        if (this.tianyi$previousPosition != null && !this.tianyi$previousPosition.equals(currentPosition)) {
            EventBus.getInstance().post(new MoveEvent(this.tianyi$previousPosition, currentPosition));
        }
    }
}