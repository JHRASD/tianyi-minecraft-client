package com.example.tianyiclient.managers;

import com.example.tianyiclient.modules.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class KeybindManager {
    private static KeybindManager instance;
    private final Map<Integer, Module> keybindMap = new HashMap<>();
    private final Map<Integer, Boolean> keyStates = new HashMap<>();

    public static KeybindManager getInstance() {
        if (instance == null) {
            instance = new KeybindManager();
        }
        return instance;
    }

    /** 初始化（需要外部调用） */
    public void init() {
        // 注册tick事件来检查按键
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;
            checkKeybinds();
        });
        System.out.println("[KeybindManager] 初始化完成");
    }

    // 注册快捷键
    public void registerKeybind(Module module, int keyCode) {
        if (keyCode == 0) {
            // 清除这个模块的所有绑定
            keybindMap.entrySet().removeIf(entry -> entry.getValue() == module);
        } else {
            // 先移除这个模块的旧绑定
            keybindMap.entrySet().removeIf(entry -> entry.getValue() == module);
            // 添加新绑定
            keybindMap.put(keyCode, module);
        }
    }

    // 检查所有快捷键
    private void checkKeybinds() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;

        for (Map.Entry<Integer, Module> entry : keybindMap.entrySet()) {
            int keyCode = entry.getKey();
            Module module = entry.getValue();

            if (keyCode == 0 || module == null) continue;

            // 检查按键状态
            long window = client.getWindow().getHandle();
            boolean isPressed = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;

            // 获取上次状态
            Boolean wasPressed = keyStates.getOrDefault(keyCode, false);

            // 按键刚按下时触发
            if (isPressed && !wasPressed) {
                module.toggle();

                // 发送提示消息
                if (client.player != null) {
                    net.minecraft.text.Text message = net.minecraft.text.Text.literal("[")
                            .append(net.minecraft.text.Text.literal("天依客户端").styled(style -> style.withColor(0xFFFF00)))
                            .append(net.minecraft.text.Text.literal("] 模块 "))
                            .append(net.minecraft.text.Text.literal(module.getName()).styled(style -> style.withColor(0xFFFFFF)))
                            .append(net.minecraft.text.Text.literal(" "))
                            .append(net.minecraft.text.Text.literal(module.isEnabled() ? "已启用" : "已禁用")
                                    .styled(style -> style.withColor(module.isEnabled() ? 0x55FF55 : 0xFF5555)));
                    client.player.sendMessage(message, true);
                }
            }

            // 更新按键状态
            keyStates.put(keyCode, isPressed);
        }
    }

    // 获取按键名称（静态方法，ClickGUI也可以调用）
    public static String getKeyName(int keyCode) {
        if (keyCode == 0) return "未绑定";

        switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE: return "空格";
            case GLFW.GLFW_KEY_ENTER: return "回车";
            case GLFW.GLFW_KEY_BACKSPACE: return "退格";
            case GLFW.GLFW_KEY_DELETE: return "DEL";
            case GLFW.GLFW_KEY_INSERT: return "INS";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PGDN";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CAPS";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
        }

        try {
            String keyName = GLFW.glfwGetKeyName(keyCode, 0);
            if (keyName != null) {
                return keyName.toUpperCase();
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return "键" + keyCode;
    }

    // 清除所有绑定
    public void clearAll() {
        keybindMap.clear();
        keyStates.clear();
    }

    // 获取模块的快捷键
    public Integer getKeybind(Module module) {
        return keybindMap.entrySet().stream()
                .filter(entry -> entry.getValue() == module)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
    }


    // 获取绑定到指定按键的模块
    public Module getModuleByKey(int keyCode) {
        return keybindMap.get(keyCode);
    }
}