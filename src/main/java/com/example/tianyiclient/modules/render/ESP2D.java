package com.example.tianyiclient.modules.render;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.render.HudRenderEvent;
import com.example.tianyiclient.utils.render.Render2DUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class ESP2D extends Module {

    public ESP2D() {
        super("ESP2D", "2D透视", Category.渲染);
    }

    @EventHandler
    public void onHudRender(HudRenderEvent event) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        DrawContext context = event.getContext();
        int y = 10;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            double distance = mc.player.distanceTo(player);
            String text = player.getName().getString() + " [" + (int)distance + "m]";

            int color;
            if (distance < 10) color = 0xFFFF0000;
            else if (distance < 30) color = 0xFFFFAA00;
            else color = 0xFF00FF00;

            Render2DUtil.drawStringWithBackground(context, text, 10, y, color, 0x88000000);
            y += 12;
        }
    }
}