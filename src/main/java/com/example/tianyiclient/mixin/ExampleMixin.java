package com.example.tianyiclient.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ExampleMixin {
	/**
	 * 注入到 MinecraftClient 的 tick() 方法开头。
	 * 这是一个简单且安全的测试注入点。
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		// 这是一个安全的测试注入点。你可以在这里添加日志来验证注入是否成功。
		// System.out.println("[ExampleMixin] MinecraftClient tick!");
	}
}