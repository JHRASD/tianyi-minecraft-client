package com.example.tianyiclient.modules.misc;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.*;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.KeyEvent;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.utils.FullScanInventoryManager;
import net.minecraft.item.*;

/**
 * 中键物品快速使用模块 - 使用工具类方法
 */
public class MiddleClickItem extends Module {

    // 设置项
    private final EnumSetting itemSetting = new EnumSetting(
            "物品选择",
            "选择要快速使用的物品",
            "烟花火箭",
            new String[]{"烟花火箭", "末影珍珠", "雪球", "鸡蛋"}
    );

    private final BoolSetting useImmediatelySetting = new BoolSetting(
            "立即使用",
            "切换到物品后自动右键使用",
            true
    );

    private final BoolSetting returnToOriginalSetting = new BoolSetting(
            "返回原槽位",
            "使用后返回原来的物品槽位",
            true
    );

    private final BoolSetting debugModeSetting = new BoolSetting(
            "调试模式",
            "在控制台显示操作信息",
            true
    );

    // 状态
    private final FullScanInventoryManager inv;
    private boolean isProcessing = false;
    private int cooldown = 0;

    public MiddleClickItem() {
        super("中键物品", "按下鼠标中键快速使用预设物品", Category.其他);
        inv = FullScanInventoryManager.getInstance();

        addSetting(itemSetting);
        addSetting(useImmediatelySetting);
        addSetting(returnToOriginalSetting);
        addSetting(debugModeSetting);

        setKeybind(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
    }

    @Override
    protected void onEnable() {
        debug("模块已启用");
        resetState();
    }

    @Override
    protected void onDisable() {
        debug("模块已禁用");
        resetState();
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        int key = event.getKey();
        int action = event.getAction();
        debug("按键事件: 键=" + key + ", 动作=" + action);

        // 检测鼠标中键
        boolean isMiddleClick = isMiddleClickKey(key);

        // 检查快捷键
        boolean isHotkey = (getKeybind() != 0 && key == getKeybind());

        if ((isMiddleClick || isHotkey) && action == 1) {
            debug("检测到触发键按下");
            handleMiddleClick();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;

        if (cooldown > 0) {
            cooldown--;
        }
    }

    /**
     * 处理中键点击
     */
    private void handleMiddleClick() {
        if (isProcessing || cooldown > 0) {
            debug("正在处理或冷却中，跳过");
            return;
        }

        // 安全检查
        if (!checkGameState()) {
            debug("游戏状态检查失败");
            return;
        }

        // 获取选择的物品
        String itemName = itemSetting.getValue();
        debug("选择的物品: " + itemName);

        Item targetItem = getItemFromName(itemName);
        if (targetItem == null) {
            debug("物品转换失败");
            return;
        }

        debug("开始执行快速使用");

        // 🎯 使用工具类的方法，一行代码搞定！
        boolean success = inv.quickUseItem(
                targetItem,
                useImmediatelySetting.getValue(),
                returnToOriginalSetting.getValue()
        );

        if (success) {
            debug("快速使用成功");
            isProcessing = true;
            cooldown = 10; // 10刻冷却
        } else {
            debug("快速使用失败");
        }
    }

    /**
     * 检测鼠标中键
     */
    private boolean isMiddleClickKey(int key) {
        // GLFW鼠标中键常量
        if (key == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return true;
        }

        // 常见的中键值
        if (key == -3 || key == 3 || key == 2) {
            return true;
        }

        return false;
    }

    /**
     * 根据名称获取物品
     */
    private Item getItemFromName(String name) {
        switch (name) {
            case "烟花火箭":
                return Items.FIREWORK_ROCKET;
            case "末影珍珠":
                return Items.ENDER_PEARL;
            case "雪球":
                return Items.SNOWBALL;
            case "鸡蛋":
                return Items.EGG;
            default:
                return null;
        }
    }

    /**
     * 检查游戏状态
     */
    private boolean checkGameState() {
        if (mc.player == null) {
            debug("玩家为空");
            return false;
        }

        if (mc.interactionManager == null) {
            debug("交互管理器为空");
            return false;
        }

        if (mc.currentScreen != null) {
            debug("正在GUI界面中");
            return false;
        }

        if (mc.player.isDead()) {
            debug("玩家已死亡");
            return false;
        }

        return true;
    }

    /**
     * 重置状态
     */
    private void resetState() {
        isProcessing = false;
        cooldown = 0;
        debug("状态已重置");
    }

    /**
     * 调试输出
     */
    private void debug(String message) {
        if (debugModeSetting.getValue()) {
            System.out.println("[中键物品] " + message);
        }
    }

    @Override
    public String getInfo() {
        return itemSetting.getValue();
    }
}