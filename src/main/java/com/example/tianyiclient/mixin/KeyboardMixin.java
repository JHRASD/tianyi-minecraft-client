package com.example.tianyiclient.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Shadow private MinecraftClient client;

    /**
     * 键盘按键事件
     * 对应 method_1466 onKey(JIIII)V
     */
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // 在这里可以发布按键事件
        // 如果需要阻止该按键的默认行为，调用 ci.cancel();
    }

    /**
     * 字符输入事件（用于处理文字输入）
     * 对应 method_1457 onChar(JII)V
     */
    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onOnChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        // 字符输入事件
    }

    /**
     * 初始化键盘（设置窗口）
     * 对应 method_1472 setup(J)V
     */
    @Inject(method = "setup", at = @At("HEAD"))
    private void onSetup(long window, CallbackInfo ci) {
        // 初始化时需要的操作
    }
}