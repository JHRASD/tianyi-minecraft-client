package com.example.tianyiclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在主菜单右下角显示 "TianyiClient 已加载"，类似 Meteor 的风格。
 * 注意：1.21.8 的 TitleScreen#render 方法签名为 render(DrawContext, int, int, float)
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void tianyiclient$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        String text = "TianyiClient 已加载";
        int color = 0xFFFFFF;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int textWidth = client.textRenderer.getWidth(text);

        int x = screenWidth - textWidth - 6; // 右下角留边距
        int y = screenHeight - client.textRenderer.fontHeight - 6;

        context.drawText(client.textRenderer, text, x, y, color, false);
    }
}
