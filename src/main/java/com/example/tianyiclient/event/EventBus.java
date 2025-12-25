package com.example.tianyiclient.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件总线 - 管理事件的注册和分发
 * 线程安全，支持优先级、事件取消和动态注册
 */
public class EventBus {
    private static EventBus instance;

    // 事件类型 -> 监听器列表的映射
    private final Map<Class<? extends Event>, List<Listener>> listeners;

    // 对象 -> 监听器列表的映射（用于快速注销）
    private final Map<Object, List<Listener>> objectListeners;

    // 锁对象，用于线程安全
    private final Object lock = new Object();

    // 是否启用调试模式
    private boolean debugMode = false;

    /**
     * 私有构造方法
     */
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.objectListeners = new ConcurrentHashMap<>();
    }

    /**
     * 获取事件总线单例
     * @return 事件总线实例
     */
    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册对象的所有事件处理器方法
     * @param object 要注册的对象
     */
    public void register(@NotNull Object object) {
        if (object == null) {
            throw new IllegalArgumentException("注册对象不能为null");
        }

        synchronized (lock) {
            // 检查是否已经注册
            if (objectListeners.containsKey(object)) {
                if (debugMode) {
                    System.out.println("[EventBus] 对象 " + object.getClass().getName() + " 已经注册");
                }
                return;
            }

            List<Listener> listenersForObject = new ArrayList<>();
            Class<?> clazz = object.getClass();

            // 遍历类及其父类的所有方法
            while (clazz != null && clazz != Object.class) {
                for (Method method : clazz.getDeclaredMethods()) {
                    EventHandler annotation = method.getAnnotation(EventHandler.class);
                    if (annotation != null) {
                        // 检查方法参数
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                            System.err.println("[EventBus] 事件处理器方法 " + method.getName() +
                                    " 必须只有一个 Event 类型参数");
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        Class<? extends Event> eventType = (Class<? extends Event>) parameterTypes[0];

                        // 创建监听器
                        Listener listener = new Listener(object, method, annotation);
                        listenersForObject.add(listener);

                        // 添加到事件类型映射
                        List<Listener> eventListeners = this.listeners
                                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());

                        // 按优先级插入
                        insertListenerByPriority(eventListeners, listener);

                        if (debugMode) {
                            System.out.println("[EventBus] 注册事件处理器: " +
                                    method.getName() + " -> " + eventType.getSimpleName() +
                                    " (优先级: " + annotation.priority() + ")");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (!listenersForObject.isEmpty()) {
                objectListeners.put(object, listenersForObject);
                if (debugMode) {
                    System.out.println("[EventBus] 成功注册对象 " + object.getClass().getName() +
                            "，包含 " + listenersForObject.size() + " 个处理器");
                }
            } else {
                System.out.println("[EventBus] 警告: 对象 " + object.getClass().getName() +
                        " 没有找到任何事件处理器");
            }
        }
    }

    /**
     * 按优先级插入监听器
     */
    private void insertListenerByPriority(List<Listener> eventListeners, Listener newListener) {
        int index = 0;
        for (; index < eventListeners.size(); index++) {
            Listener existing = eventListeners.get(index);
            if (newListener.priority.isHigherThan(existing.priority)) {
                break;
            }
        }
        eventListeners.add(index, newListener);
    }

    /**
     * 注销对象的所有事件处理器
     * @param object 要注销的对象
     */
    public void unregister(@NotNull Object object) {
        if (object == null) {
            throw new IllegalArgumentException("注销对象不能为null");
        }

        synchronized (lock) {
            List<Listener> listenersForObject = objectListeners.remove(object);
            if (listenersForObject == null) {
                if (debugMode) {
                    System.out.println("[EventBus] 对象 " + object.getClass().getName() + " 未注册");
                }
                return;
            }

            // 从所有事件类型中移除该对象的监听器
            for (Listener listener : listenersForObject) {
                List<Listener> eventListeners = this.listeners.get(listener.eventType);
                if (eventListeners != null) {
                    eventListeners.remove(listener);

                    // 如果事件类型没有监听器了，移除该事件类型
                    if (eventListeners.isEmpty()) {
                        this.listeners.remove(listener.eventType);
                    }
                }
            }

            if (debugMode) {
                System.out.println("[EventBus] 成功注销对象 " + object.getClass().getName() +
                        "，移除 " + listenersForObject.size() + " 个处理器");
            }
        }
    }

    /**
     * 发布事件到所有注册的监听器
     * @param event 要发布的事件
     * @return 事件本身（便于链式调用）
     */
    public <T extends Event> T post(@NotNull T event) {
        if (event == null) {
            throw new IllegalArgumentException("事件不能为null");
        }

        Class<? extends Event> eventType = event.getClass();
        List<Listener> eventListeners = listeners.get(eventType);

        if (eventListeners == null || eventListeners.isEmpty()) {
            return event;
        }

        if (debugMode) {
            System.out.println("[EventBus] 发布事件: " + eventType.getSimpleName() +
                    " (监听器数量: " + eventListeners.size() + ")");
        }

        // 遍历所有监听器（已按优先级排序）
        for (Listener listener : eventListeners) {
            try {
                // 检查事件是否可取消且已被取消
                boolean isCancellable = event instanceof Cancelable;
                boolean isCancelled = isCancellable && ((Cancelable) event).isCancelled();

                // 如果事件已取消且监听器不接收已取消的事件，则跳过
                if (isCancelled && !listener.receiveCancelled) {
                    if (debugMode) {
                        System.out.println("[EventBus] 跳过 " + listener.method.getName() +
                                " (事件已取消)");
                    }
                    continue;
                }

                // 如果事件传播已停止且监听器不忽略取消，则停止分发
                if (event.isPropagationStopped() && !listener.ignoreCancelled) {
                    if (debugMode) {
                        System.out.println("[EventBus] 事件传播已停止: " + eventType.getSimpleName());
                    }
                    break;
                }

                // 调用监听器方法
                if (debugMode) {
                    System.out.println("[EventBus] 调用处理器: " + listener.method.getName() +
                            " (优先级: " + listener.priority + ")");
                }

                listener.method.invoke(listener.object, event);

            } catch (Exception e) {
                System.err.println("[EventBus] 调用事件处理器失败: " + listener.method.getName());
                e.printStackTrace();
            }
        }

        return event;
    }

    /**
     * 发布事件并获取是否被取消
     * @param event 要发布的事件
     * @return 事件是否被取消（仅对可取消事件有效）
     */
    public boolean postAndCheckCancelled(@NotNull Event event) {
        if (!(event instanceof Cancelable)) {
            throw new IllegalArgumentException("事件 " + event.getClass().getName() + " 不可取消");
        }

        post(event);
        return ((Cancelable) event).isCancelled();
    }

    /**
     * 检查事件是否有监听器
     * @param eventType 事件类型
     * @return 是否有监听器
     */
    public boolean hasListeners(@NotNull Class<? extends Event> eventType) {
        List<Listener> eventListeners = listeners.get(eventType);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    /**
     * 获取指定事件类型的监听器数量
     * @param eventType 事件类型
     * @return 监听器数量
     */
    public int getListenerCount(@NotNull Class<? extends Event> eventType) {
        List<Listener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * 获取所有已注册的事件类型
     * @return 事件类型集合
     */
    public Set<Class<? extends Event>> getRegisteredEventTypes() {
        return new HashSet<>(listeners.keySet());
    }

    /**
     * 获取所有已注册的对象
     * @return 已注册对象集合
     */
    public Set<Object> getRegisteredObjects() {
        return new HashSet<>(objectListeners.keySet());
    }

    /**
     * 清理所有注册的监听器
     */
    public void clear() {
        synchronized (lock) {
            listeners.clear();
            objectListeners.clear();
            if (debugMode) {
                System.out.println("[EventBus] 已清理所有监听器");
            }
        }
    }

    /**
     * 设置调试模式
     * @param debug 是否启用调试模式
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    /**
     * 获取调试模式状态
     * @return 是否启用调试模式
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 订阅事件（函数式编程风格）
     * @param eventType 事件类型
     * @param handler 事件处理器
     * @param priority 优先级
     * @param <T> 事件类型
     * @return 订阅ID，可用于取消订阅
     */
    public <T extends Event> Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull Consumer<T> handler,
            @NotNull Priority priority) {

        Object subscriber = new Object();
        Method method;
        try {
            method = subscriber.getClass().getDeclaredMethod("invoke", Event.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("创建订阅失败", e);
        }

        EventHandler annotation = new EventHandler() {
            @Override
            public Priority priority() {
                return priority;
            }

            @Override
            public boolean receiveCancelled() {
                return false;
            }

            @Override
            public boolean ignoreCancelled() {
                return false;
            }

            @Override
            public String id() {
                return "functional-" + System.identityHashCode(handler);
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return EventHandler.class;
            }
        };

        // 使用动态代理包装处理器
        Object proxy = new Object() {
            @SuppressWarnings("unused")
            public void invoke(Event event) {
                handler.accept(eventType.cast(event));
            }
        };

        register(proxy);
        return new Subscription(proxy);
    }

    /**
     * 订阅事件（默认优先级）
     */
    public <T extends Event> Subscription subscribe(
            @NotNull Class<T> eventType,
            @NotNull Consumer<T> handler) {
        return subscribe(eventType, handler, Priority.NORMAL);
    }

    /**
     * 订阅ID类
     */
    public static class Subscription {
        private final Object subscriber;

        private Subscription(Object subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * 取消订阅
         */
        public void unsubscribe() {
            getInstance().unregister(subscriber);
        }
    }

    /**
     * 事件监听器包装类
     */
    private static class Listener {
        final Object object;
        final Method method;
        final Class<? extends Event> eventType;
        final Priority priority;
        final boolean receiveCancelled;
        final boolean ignoreCancelled;
        final String id;

        Listener(Object object, Method method, EventHandler annotation) {
            this.object = object;
            this.method = method;
            this.method.setAccessible(true);
            this.eventType = (Class<? extends Event>) method.getParameterTypes()[0];
            this.priority = annotation.priority();
            this.receiveCancelled = annotation.receiveCancelled();
            this.ignoreCancelled = annotation.ignoreCancelled();
            this.id = annotation.id();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Listener listener = (Listener) o;
            return Objects.equals(object, listener.object) &&
                    Objects.equals(method, listener.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(object, method);
        }
    }
}