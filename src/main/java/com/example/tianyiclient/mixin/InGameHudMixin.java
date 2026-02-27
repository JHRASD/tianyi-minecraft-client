package com.example.tianyiclient.mixin;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.event.events.render.HudRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // 获取 tickDelta，如果不知道具体方法，先传 1.0f
        float tickDelta = 1.0f;

        // 发布事件，使用 MAIN 阶段
        HudRenderEvent event = new HudRenderEvent(
                context,
                tickDelta,
                HudRenderEvent.Phase.MAIN,
                screenWidth,
                screenHeight
        );
        TianyiClient.EVENT_BUS.post(event);
    }
}