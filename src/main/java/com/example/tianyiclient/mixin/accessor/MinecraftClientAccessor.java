// mixin/accessor/MinecraftClientAccessor.java
package com.example.tianyiclient.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);

    @Accessor("itemUseCooldown")
    int getItemUseCooldown();

    @Accessor("attackCooldown")
    void setAttackCooldown(int cooldown);

    @Accessor("attackCooldown")
    int getAttackCooldown();
}