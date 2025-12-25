package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

/**
 * 简洁透明版性能监控
 */
public class PerformanceMonitorElement extends HudElement {

    private final SystemInfo systemInfo;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final MemoryMXBean memoryBean;
    private long[] prevTicks;
    private long lastCpuUpdate = 0;
    private double cpuUsage = 0;
    private long lastMemUpdate = 0;
    private long usedMB = 0;
    private long maxMB = 0;

    public PerformanceMonitorElement() {
        super("性能监控", 600.0f, 10.0f);

        // 初始化OSHI
        systemInfo = new SystemInfo();
        processor = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        memoryBean = ManagementFactory.getMemoryMXBean();
        prevTicks = processor.getSystemCpuLoadTicks();

        // 初始化内存数据
        updateMemoryData();
        setSize(100, 35); // 更小尺寸
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isVisible() || mc == null) return;

        updatePerformanceData();
        int x = (int) getX();
        int y = (int) getY();

        // 直接绘制性能指标（无背景，无标题）
        drawCompactMetrics(context, x, y);
    }

    /**
     * 绘制紧凑性能指标
     */
    private void drawCompactMetrics(DrawContext context, int x, int y) {
        DecimalFormat df = new DecimalFormat("0");
        int lineHeight = 11; // 更紧凑的行高
        int currentY = y;

        // 1. CPU
        String cpuText = String.format("cpu%s%%", df.format(cpuUsage));
        int cpuColor = getCpuColor(cpuUsage);
        context.drawText(mc.textRenderer, cpuText, x, currentY, cpuColor, true);

        // 简短的CPU进度条
        drawMiniBar(context, x + mc.textRenderer.getWidth(cpuText) + 2, currentY + 2,
                (int) (cpuUsage / 100 * 20), 2, cpuColor);
        currentY += lineHeight;

        // 2. 内存
        String memText = String.format("内存%dM", usedMB);
        int memColor = getMemoryColor((double) usedMB / maxMB * 100);
        context.drawText(mc.textRenderer, memText, x, currentY, memColor, true);

        // 简短的内存进度条
        drawMiniBar(context, x + mc.textRenderer.getWidth(memText) + 2, currentY + 2,
                (int) ((double) usedMB / maxMB * 20), 2, memColor);
        currentY += lineHeight;

        // 3. FPS
        int fps = mc.getCurrentFps();
        String fpsText = String.format("fps%d", fps);
        int fpsColor = getFpsColor(fps);
        context.drawText(mc.textRenderer, fpsText, x, currentY, fpsColor, true);

        // 简短的FPS进度条
        double fpsPercent = Math.min((double) fps / 60 * 100, 100);
        drawMiniBar(context, x + mc.textRenderer.getWidth(fpsText) + 2, currentY + 2,
                (int) (fpsPercent / 100 * 20), 2, fpsColor);

        // 自动调整面板大小
        int maxWidth = Math.max(
                mc.textRenderer.getWidth(cpuText),
                Math.max(
                        mc.textRenderer.getWidth(memText),
                        mc.textRenderer.getWidth(fpsText)
                )
        ) + 25; // 为进度条留出空间
        setSize(maxWidth, lineHeight * 3);
    }

    /**
     * 绘制迷你进度条
     */
    private void drawMiniBar(DrawContext context, int x, int y, int width, int height, int color) {
        // 背景
        context.fill(x, y, x + 20, y + height, 0x44FFFFFF);
        // 进度
        if (width > 0) {
            context.fill(x, y, x + width, y + height, color);
        }
    }

    /**
     * 根据性能数据获取动态颜色
     */
    private int getCpuColor(double usage) {
        if (usage < 50) return 0xFF00FF96; // 绿色
        if (usage < 80) return 0xFFFFD166; // 黄色
        return 0xFFFF6B6B; // 红色
    }

    private int getMemoryColor(double percent) {
        if (percent < 60) return 0xFF00CEC9; // 青色
        if (percent < 85) return 0xFFFFD166; // 黄色
        return 0xFFFF6B6B; // 红色
    }

    private int getFpsColor(int fps) {
        if (fps >= 60) return 0xFF00FF96; // 绿色
        if (fps >= 30) return 0xFFFFD166; // 黄色
        return 0xFFFF6B6B; // 红色
    }

    /**
     * 更新性能数据
     */
    private void updatePerformanceData() {
        long currentTime = System.currentTimeMillis();

        // 更新CPU (每秒)
        if (currentTime - lastCpuUpdate > 1000) {
            cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
            prevTicks = processor.getSystemCpuLoadTicks();
            lastCpuUpdate = currentTime;
        }

        // 更新内存 (每2秒)
        if (currentTime - lastMemUpdate > 2000) {
            updateMemoryData();
            lastMemUpdate = currentTime;
        }
    }

    /**
     * 更新内存数据
     */
    private void updateMemoryData() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        usedMB = heapUsage.getUsed() / (1024 * 1024);
        maxMB = heapUsage.getMax() / (1024 * 1024);
    }
}