package com.example.tianyiclient.mixin;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.events.client.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        TickEvent event = new TickEvent(TickEvent.Phase.START);
        EventBus.getInstance().post(event);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onPostTick(CallbackInfo info) {
        TickEvent event = new TickEvent(TickEvent.Phase.END);
        EventBus.getInstance().post(event);
    }
}