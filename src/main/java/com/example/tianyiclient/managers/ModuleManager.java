package com.example.tianyiclient.managers;

import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private KeybindManager keybindManager;

    /** 初始化管理器（在主客户端类中调用） */
    public void init() {
        // 初始化KeybindManager
        keybindManager = KeybindManager.getInstance();
        keybindManager.init();

        // 注册所有模块的快捷键
        registerAllKeybinds();

        System.out.println("[ModuleManager] 初始化完成");
    }

    /** 注册模块 */
    public void register(Module module) {
        modules.add(module);
        System.out.println("[ModuleManager] 注册模块: " + module.getName());

        // 新增：将模块注册到EventBus
        EventBus.getInstance().register(module);
        System.out.println("[ModuleManager] 模块注册到EventBus: " + module.getName());

        // 注册模块的快捷键（如果存在）
        if (module.getKeybind() != 0 && keybindManager != null) {
            keybindManager.registerKeybind(module, module.getKeybind());
        }
    }

    /** 注册所有模块的快捷键 */
    private void registerAllKeybinds() {
        if (keybindManager == null) return;

        for (Module module : modules) {
            if (module.getKeybind() != 0) {
                keybindManager.registerKeybind(module, module.getKeybind());
            }
        }
        System.out.println("[ModuleManager] 注册了 " + modules.size() + " 个模块的快捷键");
    }

    /** 根据名称获取模块 */
    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    /** 获取某个分类的模块列表 */
    public List<Module> getModulesByCategory(Category category) {
        List<Module> list = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) list.add(m);
        }
        return list;
    }

    /** 获取全部模块 */
    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    /** 根据快捷键获取模块 */
    public Module getModuleByKeybind(int keyCode) {
        if (keybindManager != null) {
            return keybindManager.getModuleByKey(keyCode);
        }
        return null;
    }

    /** 获取KeybindManager实例 */
    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    /** 重新注册所有快捷键（用于配置文件重新加载） */
    public void reloadKeybinds() {
        if (keybindManager != null) {
            keybindManager.clearAll();
            registerAllKeybinds();
        }
    }
}