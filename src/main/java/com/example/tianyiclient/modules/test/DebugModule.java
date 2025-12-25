package com.example.tianyiclient.modules.test;

import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.Priority;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.modules.Module;

public class DebugModule extends Module {

    private long lastTickTime = 0;
    private int tickCount = 0;

    public DebugModule() {
        super("调试模块", "显示系统状态", Category.其他);
    }

    @EventHandler(priority = Priority.LOW)
    public void onTick(TickEvent event) {
        tickCount++;

        // 每100个tick输出一次统计
        if (tickCount % 100 == 0) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastTickTime;

            if (lastTickTime != 0 && elapsed > 0) {
                double tps = 100.0 / (elapsed / 1000.0);
                System.out.printf("[Debug] TPS: %.2f, Total Ticks: %d%n", tps, tickCount);
            }

            lastTickTime = currentTime;
        }
    }
}