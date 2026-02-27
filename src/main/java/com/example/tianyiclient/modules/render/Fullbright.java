package com.example.tianyiclient.modules.render;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {

    private double previousGamma;
    private boolean hadEffect;

    public Fullbright() {
        super("夜视", "夜视", Category.渲染);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;

        previousGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(16.0);

        // 添加夜视效果（无粒子、无图标）
        mc.player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                -1, 0, false, false, false
        ));
        hadEffect = true;
    }

    @Override
    protected void onDisable() {
        if (mc.player == null) return;

        mc.options.getGamma().setValue(previousGamma);

        if (hadEffect) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            hadEffect = false;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled() || mc.player == null) return;

        // 确保夜视效果一直存在且无图标
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, -1, 0, false, false, false
            ));
        }

        if (mc.options.getGamma().getValue() < 16.0) {
            mc.options.getGamma().setValue(16.0);
        }
    }
}