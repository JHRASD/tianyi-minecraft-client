package com.example.tianyiclient.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法作为事件处理器
 * 被注解的方法会在相应事件发布时被调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * 事件处理器的优先级
     * @return 优先级，默认为NORMAL
     */
    Priority priority() default Priority.NORMAL;

    /**
     * 是否接收已取消的事件
     * 如果为false，当事件被取消时不会调用此处理器
     * @return 是否接收已取消的事件，默认为false
     */
    boolean receiveCancelled() default false;

    /**
     * 是否在事件传播停止后仍然接收
     * 如果为true，即使事件被取消也会被调用（需要配合receiveCancelled）
     * @return 是否忽略传播停止，默认为false
     */
    boolean ignoreCancelled() default false;

    /**
     * 事件处理器的唯一标识符（可选）
     * 用于动态启用/禁用特定处理器
     * @return 处理器ID，默认为空字符串
     */
    String id() default "";
}