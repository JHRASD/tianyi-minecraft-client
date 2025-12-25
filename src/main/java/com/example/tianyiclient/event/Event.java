package com.example.tianyiclient.event;

/**
 * 所有事件的抽象基类
 * 事件系统采用发布-订阅模式，支持事件优先级和取消机制
 */
public abstract class Event {
    private boolean cancelled = false;

    /**
     * 检查事件是否可取消
     * @return 如果是可取消事件返回true
     */
    public boolean isCancellable() {
        return this instanceof Cancelable;
    }

    /**
     * 获取事件是否已被取消（仅对可取消事件有效）
     * @return 事件是否被取消
     */
    public boolean isCancelled() {
        return isCancellable() && cancelled;
    }

    /**
     * 设置事件取消状态（仅对可取消事件有效）
     * @param cancelled 是否取消事件
     * @throws UnsupportedOperationException 如果事件不可取消
     */
    public void setCancelled(boolean cancelled) {
        if (!isCancellable()) {
            throw new UnsupportedOperationException("事件 " + getClass().getName() + " 不支持取消操作");
        }
        this.cancelled = cancelled;
    }

    /**
     * 取消事件（便捷方法）
     */
    public void cancel() {
        setCancelled(true);
    }

    /**
     * 获取事件名称，用于调试和日志
     * @return 事件的简单类名
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 获取事件是否已传播完成
     * @return 如果是广播事件，返回是否已传播到所有监听器
     */
    public boolean isPropagationStopped() {
        return isCancelled();
    }

    /**
     * 停止事件传播（取消事件）
     */
    public void stopPropagation() {
        if (isCancellable()) {
            cancel();
        }
    }

    @Override
    public String toString() {
        return String.format("%s{cancelled=%s}", getName(), cancelled);
    }
}