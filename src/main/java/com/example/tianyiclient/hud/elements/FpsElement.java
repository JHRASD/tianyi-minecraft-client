package com.example.tianyiclient.hud.elements;

import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.hud.binding.DataBinding;
import com.example.tianyiclient.hud.binding.DataProviderRegistry;
import com.example.tianyiclient.settings.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 显示FPS、内存、时间等信息
 */
public class FpsElement extends TextHudElement {

    public FpsElement() {
        super("FPS信息", 10.0f, 30.0f); // 默认位置在水印下面

        // 设置默认文本
        setText("⚡${fps} | 🧠${memory} | 🕐${time}");

        // 添加额外设置
        getStyleGroup().add(new BoolSetting("显示内存", "FPS - 显示内存使用", true));
        getStyleGroup().add(new BoolSetting("显示时间", "FPS - 显示当前时间", true));
        getStyleGroup().add(new EnumSetting("时间格式", "FPS - 时间显示格式", "HH:mm",
                new String[]{"HH:mm", "HH:mm:ss", "hh:mm a"}));

        // 添加自定义数据绑定
        addDataBinding(new DataBinding("${time}", () -> {
            String format = getSettingValue(getStyleGroup(), "时间格式", String.class, "HH:mm");
            return new SimpleDateFormat(format).format(new Date());
        }));

        addDataBinding(new DataBinding("${memory}", () -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            long totalMB = runtime.totalMemory() / 1024 / 1024;
            return String.format("%d/%dMB", usedMB, totalMB);
        }));

        // 设置默认颜色和样式
        setColor(0xFFFFFFFF);
        setShadow(true);
        setFontSize(9);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        // 更新显示文本
        String displayText = buildDisplayText();
        setText(displayText);

        // 调用父类渲染
        super.render(context, tickDelta);
    }

    private String buildDisplayText() {
        StringBuilder sb = new StringBuilder();

        // FPS（总是显示）
        sb.append("⚡${fps}");

        // 内存（根据设置）
        Boolean showMemory = getSettingValue(getStyleGroup(), "显示内存", Boolean.class, true);
        if (showMemory) {
            sb.append(" | 🧠${memory}");
        }

        // 时间（根据设置）
        Boolean showTime = getSettingValue(getStyleGroup(), "显示时间", Boolean.class, true);
        if (showTime) {
            sb.append(" | 🕐${time}");
        }

        return sb.toString();
    }
}