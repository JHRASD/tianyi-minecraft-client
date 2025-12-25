package com.example.tianyiclient.event.events;

import com.example.tianyiclient.event.Event;

public class RenderEvent extends Event {
    public final float tickDelta;

    public RenderEvent(float tickDelta) {
        this.tickDelta = tickDelta;
    }
}
