package com.example.tianyiclient.modules.test;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.client.KeyEvent;
import com.example.tianyiclient.event.events.client.TickEvent;

/**
 * 用于测试EventBus的简单类
 */
public class EventBusTest {

    private static boolean isTesting = false;

    /**
     * 开始测试EventBus
     */
    public static void startTest() {
        if (isTesting) return;

        EventBus bus = EventBus.getInstance();
        bus.setDebugMode(true); // 开启调试模式

        // 注册测试监听器
        TestListener listener = new TestListener();
        bus.register(listener);

        isTesting = true;
        System.out.println("[EventBusTest] EventBus测试已启动");
    }

    /**
     * 停止测试
     */
    public static void stopTest() {
        if (!isTesting) return;

        EventBus bus = EventBus.getInstance();
        bus.setDebugMode(false);

        isTesting = false;
        System.out.println("[EventBusTest] EventBus测试已停止");
    }

    /**
     * 测试监听器内部类
     */
    private static class TestListener {

        private int tickCount = 0;

        @EventHandler(priority = Priority.HIGH)
        public void onKeyEvent(KeyEvent event) {
            System.out.printf("[EventBusTest] 按键事件: %s%n", event.getNormalizedKeyName());

            // 测试事件取消
            if (event.getKeyCode() == 123) { // F12
                event.setCancelled(true);
                System.out.println("[EventBusTest] F12按键事件已被取消");
            }
        }

        @EventHandler(priority = Priority.NORMAL)
        public void onTickEvent(TickEvent event) {
            tickCount++;
            if (tickCount % 100 == 0) {
                System.out.printf("[EventBusTest] 已处理 %d 个Tick事件%n", tickCount);
            }
        }
    }
}