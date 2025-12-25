package com.example.tianyiclient.event.events.render;

import com.example.tianyiclient.event.Event;
import net.minecraft.client.gui.DrawContext;

/**
 * HUD渲染事件
 * 在游戏渲染HUD时触发，用于绘制自定义界面元素
 */
public class HudRenderEvent extends Event {
    private final DrawContext context;
    private final float tickDelta;
    private final Phase phase;
    private final int screenWidth;
    private final int screenHeight;

    public HudRenderEvent(DrawContext context, float tickDelta, Phase phase, int screenWidth, int screenHeight) {
        this.context = context;
        this.tickDelta = tickDelta;
        this.phase = phase;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public DrawContext getContext() {
        return context;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }


    /**
     * HUD渲染阶段
     */
    public enum Phase {
        /** 渲染开始 */
        PRE,
        /** 渲染主要阶段 */
        MAIN,
        /** 渲染结束 */
        POST
    }
}