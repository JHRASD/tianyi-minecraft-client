package com.example.tianyiclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private MinecraftClient client;

    /**
     * 鼠标按键事件
     * 对应 method_1601 onMouseButton(JIII)V
     */
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onOnMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        // 处理鼠标点击事件
    }

    /**
     * 鼠标移动事件
     * 对应 method_1600 onCursorPos(JDD)V
     */
    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void onOnCursorPos(long window, double x, double y, CallbackInfo ci) {
        // 处理鼠标移动
    }

    /**
     * 鼠标滚轮事件
     * 对应 method_1598 onMouseScroll(JDD)V
     */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        // 处理滚轮
    }

    /**
     * 初始化鼠标（设置窗口）
     * 对应 method_1607 setup(J)V
     */
    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetup(long window, CallbackInfo ci) {
        // 初始化
    }
}