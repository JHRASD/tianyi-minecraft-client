package com.example.tianyiclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow private int itemUseCooldown;
    @Shadow private Screen currentScreen;
    // 可以继续添加更多需要的字段

    /**
     * 获取物品使用冷却时间
     */
    public int getItemUseCooldown() {
        return this.itemUseCooldown;
    }

    /**
     * 设置物品使用冷却时间
     */
    public void setItemUseCooldown(int cooldown) {
        this.itemUseCooldown = cooldown;
    }

    /**
     * 获取当前屏幕
     */
    public Screen getCurrentScreen() {
        return this.currentScreen;
    }

    /**
     * 在客户端 tick 时注入（对应 method_1574 tick()V）
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // 在这里发布 TickEvent，或直接执行模块逻辑
    }

    /**
     * 在设置屏幕时注入（对应 method_1507 setScreen(Lnet/minecraft/class_437;)V）
     */
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // 屏幕切换事件
    }

    /**
     * 在客户端关闭时注入（对应 method_1490 stop()V）
     */
    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        // 清理资源
    }
}