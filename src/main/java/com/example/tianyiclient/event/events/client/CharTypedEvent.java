package com.example.tianyiclient.event.events.client;

import com.example.tianyiclient.event.Cancelable;
import com.example.tianyiclient.event.Event; /**
 * CharTypedEvent - 字符输入事件
 * 当用户输入字符时触发（考虑输入法、组合键等）
 */
public class CharTypedEvent extends Event implements Cancelable {
    private final char character;
    private final int modifiers;
    private boolean cancelled = false;

    public CharTypedEvent(char character, int modifiers) {
        this.character = character;
        this.modifiers = modifiers;
    }

    public char getCharacter() {
        return character;
    }

    public int getModifiers() {
        return modifiers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public String toString() {
        return String.format("CharTypedEvent{char='%c'(%d), modifiers=%s, cancelled=%s}",
                character, (int) character, Integer.toBinaryString(modifiers), cancelled);
    }
}
