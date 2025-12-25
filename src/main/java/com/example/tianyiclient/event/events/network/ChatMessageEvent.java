package com.example.tianyiclient.event.events.network;

import com.example.tianyiclient.event.Event;
import com.example.tianyiclient.event.Cancelable;
import net.minecraft.text.Text;

/**
 * 聊天消息事件
 * 当收到或发送聊天消息时触发
 */
public class ChatMessageEvent extends Event implements Cancelable {
    private final String message;
    private final Text text;
    private final MessageType type;
    private final String sender;
    private boolean cancelled = false;

    /**
     * 消息类型
     */
    public enum MessageType {
        /**
         * 收到聊天消息
         */
        INCOMING,

        /**
         * 发送聊天消息
         */
        OUTGOING,

        /**
         * 系统消息
         */
        SYSTEM,

        /**
         * 游戏内命令
         */
        COMMAND
    }

    public ChatMessageEvent(String message, Text text, MessageType type, String sender) {
        this.message = message;
        this.text = text;
        this.type = type;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public Text getText() {
        return text;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    /**
     * 检查是否为传入消息
     */
    public boolean isIncoming() {
        return type == MessageType.INCOMING;
    }

    /**
     * 检查是否为传出消息
     */
    public boolean isOutgoing() {
        return type == MessageType.OUTGOING;
    }

    /**
     * 检查是否为系统消息
     */
    public boolean isSystem() {
        return type == MessageType.SYSTEM;
    }

    /**
     * 检查是否为命令
     */
    public boolean isCommand() {
        return type == MessageType.COMMAND;
    }

    /**
     * 检查消息是否以指定前缀开头
     */
    public boolean startsWith(String prefix) {
        return message.startsWith(prefix);
    }

    /**
     * 检查消息是否包含指定文本
     */
    public boolean contains(String text) {
        return message.contains(text);
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
        return String.format("ChatMessageEvent{type=%s, sender='%s', message='%s'}",
                type, sender, message.length() > 50 ? message.substring(0, 47) + "..." : message);
    }
}