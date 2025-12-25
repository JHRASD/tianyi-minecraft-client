package com.example.tianyiclient.mixin;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.events.render.HudRenderEvent;
import com.example.tianyiclient.hud.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * 注入到HUD渲染方法中
     * 正确签名：render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
     * 获取tickDelta：tickCounter.getTickProgress(true)
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        // 正确获取tickDelta的方法
        float tickDelta = tickCounter.getTickProgress(true);

        // 发布HUD渲染事件
        EventBus.getInstance().post(new HudRenderEvent(
                context,
                tickDelta,
                HudRenderEvent.Phase.MAIN,
                screenWidth,
                screenHeight
        ));

        // 调用HUD渲染器
        HudRenderer.getInstance().render(context, tickDelta);
    }
}