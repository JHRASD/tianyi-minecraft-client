package com.example.tianyiclient.modules.render;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.*;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {

    // 方案2：纯Mixin实现，无需设置项
    public Fullbright() {
        super("夜视", "修改光照表实现真正的夜视效果", Category.渲染);
        setKeybind(GLFW.GLFW_KEY_N);

        // 可选：添加亮度调节设置
        addSetting(new DoubleSetting(
                "亮度",
                "光照亮度",
                1.0,
                0.1,
                2.0
        ));
    }
}