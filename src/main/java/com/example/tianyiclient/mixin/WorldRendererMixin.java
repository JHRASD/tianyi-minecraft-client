package com.example.tianyiclient.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderEntities", at = @At("RETURN"))
    private void onRenderEntities(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                  Camera camera, RenderTickCounter tickCounter, List<Entity> entities,
                                  CallbackInfo ci) {
        // 在这里绘制 ESP、Tracers 等
    }
}