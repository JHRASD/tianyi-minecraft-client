// mixin/LightmapTextureManagerMixin.java
package com.example.tianyiclient.mixin;

import com.example.tianyiclient.modules.render.Fullbright;
import com.example.tianyiclient.TianyiClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class GameRendererMixin {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(CallbackInfoReturnable<Float> cir) {
        // 检查Fullbright模块是否启用
        Fullbright module = (Fullbright) TianyiClient.getInstance()
                .getModuleManager()
                .getModuleByName("夜视");

        if (module != null && module.isEnabled()) {
            // 返回最大亮度
            cir.setReturnValue(1.0F);
        }
    }
}