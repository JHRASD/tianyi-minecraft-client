package com.example.tianyiclient.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;  // 修正：Session 在 client.session 包下
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface IMinecraftClientAccessor {
    @Accessor("currentFps")
    int getCurrentFps();

    @Accessor("itemUseCooldown")
    int getItemUseCooldown();

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);

    @Accessor("session")  // 字段名正确
    void setSession(Session session);

    @Invoker("doAttack")
    boolean invokeDoAttack();

    @Invoker("doItemUse")
    void invokeDoItemUse();
}