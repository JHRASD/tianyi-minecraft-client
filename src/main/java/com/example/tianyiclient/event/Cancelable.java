package com.example.tianyiclient.event;

/**
 * 标记一个事件可以被取消
 * 实现此接口的事件可以被监听器取消，阻止后续处理
 */
public interface Cancelable {
    /**
     * 检查事件是否已被取消
     * @return 如果事件被取消返回true
     */
    boolean isCancelled();

    /**
     * 设置事件取消状态
     * @param cancelled 是否取消事件
     */
    void setCancelled(boolean cancelled);

    /**
     * 取消事件（便捷方法）
     */
    default void cancel() {
        setCancelled(true);
    }
}