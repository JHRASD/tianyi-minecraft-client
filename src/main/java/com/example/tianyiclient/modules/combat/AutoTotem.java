package com.example.tianyiclient.modules.combat;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.*;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.utils.FullScanInventoryManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;

import java.util.List;

public class AutoTotem extends Module {

    private final FullScanInventoryManager inv;
    private boolean emergencyMode = false;
    private int totemCount = 0;
    private int reactionCooldown = 0;

    // 🆕 热键栏图腾系统
    private int hotbarTotemSlot = -1;               // 热键栏中的图腾槽位（-1表示没有）
    private boolean hotbarTotemActive = false;      // 是否激活了热键栏图腾
    private int originalSelectedSlot = -1;          // 玩家原来的选中槽位
    private boolean shouldRestoreSelectedSlot = false; // 是否需要恢复选中槽位
    private int hotbarTotemCooldown = 0;            // 热键栏图腾操作冷却

    // 设置项引用
    private DoubleSetting healthThresholdSlider;
    private DoubleSetting tntMinecartRangeSlider;
    private BoolSetting hotbarTotemSetting;         // 🆕 热键栏图腾设置

    public AutoTotem() {
        super("自动图腾", "在危险时自动装备不死图腾到副手或热键栏", Category.战斗);
        inv = FullScanInventoryManager.getInstance();
    }

    @Override
    protected void init() {
        // 布尔设置（开关）
        addSetting(new BoolSetting(
                "始终激活", "永久保持有图腾可用", true
        ));

        // 🆕 热键栏图腾设置
        hotbarTotemSetting = addSetting(new BoolSetting(
                "热键栏图腾",
                "副手被占用时，将图腾放入热键栏并切换主手",
                true
        ));

        addSetting(new BoolSetting(
                "极速模式", "快速交换物品", true
        ));

        // 🎯 滑块设置：生命阈值（1-20）
        healthThresholdSlider = addSetting(new DoubleSetting(
                "生命阈值",
                "低于此值强制装备",
                10.0,   // 默认值
                1.0,    // 最小值
                20.0    // 最大值
        ));

        addSetting(new BoolSetting(
                "TNT矿车检测", "检测附近TNT矿车", true
        ));

        // 🎯 滑块设置：TNT矿车检测范围（5-30）
        tntMinecartRangeSlider = addSetting(new DoubleSetting(
                "检测范围",
                "TNT矿车检测范围",
                12.0,   // 默认值
                5.0,    // 最小值
                30.0    // 最大值
        ));

        addSetting(new BoolSetting(
                "紧急反应", "检测到危险立即装备", true
        ));

        addSetting(new BoolSetting(
                "优先热键栏", "优先使用热键栏的图腾", true
        ));

        // 滑块设置：反应延迟（0-10 tick）
        addSetting(new DoubleSetting(
                "反应延迟",
                "检测到危险后的反应延迟",
                2.0,    // 默认值
                0.0,    // 最小值
                10.0    // 最大值
        ));

        // 滑块设置：冷却时间（1-20 tick）
        addSetting(new DoubleSetting(
                "冷却时间",
                "装备图腾后的冷却时间",
                5.0,    // 默认值
                1.0,    // 最小值
                20.0    // 最大值
        ));

        // 滑块设置：检查间隔（1-20 tick）
        addSetting(new DoubleSetting(
                "检查间隔",
                "检查图腾的间隔时间",
                3.0,    // 默认值
                1.0,    // 最小值
                20.0    // 最大值
        ));

        // 🆕 滑块设置：热键栏优先级
        addSetting(new DoubleSetting(
                "热键栏优先级",
                "从哪个热键栏槽位开始选择",
                7.0,    // 默认值（从右数第2个）
                0.0,    // 最小值
                8.0     // 最大值
        ));

        // 布尔设置：显示调试信息
        addSetting(new BoolSetting(
                "调试模式", "在控制台显示调试信息", false
        ));

        // 设置快捷键
        setKeybind(org.lwjgl.glfw.GLFW.GLFW_KEY_G);
    }

    @Override
    protected void onEnable() {
        boolean debugMode = getBoolSettingValue("调试模式");
        inv.setDebugMode(debugMode);

        emergencyMode = false;
        reactionCooldown = 0;
        hotbarTotemCooldown = 0;
        hotbarTotemSlot = -1;
        hotbarTotemActive = false;
        originalSelectedSlot = -1;
        shouldRestoreSelectedSlot = false;
        clearDisplayInfo();

        System.out.println("[AutoTotem] 模块已启用");
    }

    @Override
    protected void onDisable() {
        inv.setDebugMode(false);

        // 🆕 禁用时恢复玩家原来的选中槽位
        if (shouldRestoreSelectedSlot && originalSelectedSlot != -1) {
            restoreOriginalSelectedSlot();
        }

        clearDisplayInfo();
        System.out.println("[AutoTotem] 模块已禁用");
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        // 更新冷却
        if (reactionCooldown > 0) reactionCooldown--;
        if (hotbarTotemCooldown > 0) hotbarTotemCooldown--;

        // 🎯 检查间隔控制
        double checkInterval = getDoubleSettingValue("检查间隔");
        if (checkInterval > 1 && mc.player.age % (int)checkInterval != 0) {
            return;
        }

        // 更新图腾数量
        totemCount = inv.countAllTotems();

        // 检查副手状态
        ItemStack offhand = mc.player.getOffHandStack();
        boolean hasTotemInOffhand = inv.isTotem(offhand);
        boolean offhandOccupied = !offhand.isEmpty() && !hasTotemInOffhand;

        // 调试信息
        if (getBoolSettingValue("调试模式") && mc.player.age % 40 == 0) {
            System.out.println("[AutoTotem] 状态 - 图腾:" + totemCount +
                    " 副手图腾:" + hasTotemInOffhand +
                    " 副手占用:" + offhandOccupied +
                    " 热键栏图腾槽位:" + hotbarTotemSlot +
                    " 热键栏图腾激活:" + hotbarTotemActive);
        }

        // 🆕 检查热键栏图腾是否还在
        if (hotbarTotemActive && hotbarTotemSlot != -1) {
            checkHotbarTotemStatus();
        }

        // 检查玩家是否手动把图腾放入了副手
        if (hasTotemInOffhand && hotbarTotemActive) {
            // 玩家手动把图腾放入了副手，清理热键栏图腾状态
            cleanupHotbarTotem();
        }

        // 检查危险条件
        boolean shouldEquip = checkDangerConditions();

        // 执行装备图腾的逻辑
        if (shouldEquip && totemCount > 0 && reactionCooldown == 0) {
            // 检查反应延迟
            double reactionDelay = getDoubleSettingValue("反应延迟");
            boolean shouldReact = (reactionDelay <= 0) || (mc.player.age % (int)reactionDelay == 0);

            if (shouldReact) {
                if (!hasTotemInOffhand) {
                    // 副手没有图腾，需要装备
                    if (offhandOccupied && hotbarTotemSetting.getValue()) {
                        // 🆕 情况1：副手被占用，使用热键栏图腾方案
                        equipTotemToHotbar();
                    } else {
                        // 🆕 情况2：副手空闲，直接装备到副手
                        equipTotemToOffhand();
                    }
                }
                // 情况3：副手已经有图腾，什么都不做
            }
        }

        // 每20tick重置紧急模式
        if (mc.player.age % 20 == 0 && !isInDanger()) {
            emergencyMode = false;
        }

        // 更新显示信息
        updateDisplayInfo();
    }

    /**
     * 🆕 将图腾装备到副手（传统方法）
     */
    private boolean equipTotemToOffhand() {
        boolean success = inv.equipTotemToOffhand();
        if (success) {
            double cooldownTime = getDoubleSettingValue("冷却时间");
            reactionCooldown = (int) Math.max(1, cooldownTime);

            // 🆕 如果之前有热键栏图腾，现在清理掉
            if (hotbarTotemActive) {
                cleanupHotbarTotem();
            }

            if (getBoolSettingValue("调试模式")) {
                System.out.println("[AutoTotem] 图腾已装备到副手");
            }
        }
        return success;
    }

    /**
     * 🆕 将图腾装备到热键栏并切换主手（新方案）
     */
    private boolean equipTotemToHotbar() {
        if (hotbarTotemCooldown > 0) return false;
        if (totemCount <= 0) return false;

        // 检查是否已经有热键栏图腾
        if (hotbarTotemActive && hotbarTotemSlot != -1) {
            // 已经有热键栏图腾，直接切换到那个槽位
            if (getSelectedHotbarSlot() != hotbarTotemSlot) {
                setSelectedHotbarSlot(hotbarTotemSlot);
            }
            return true;
        }

        // 查找热键栏中的图腾
        int totemInHotbar = findTotemInHotbar();
        if (totemInHotbar != -1) {
            // 热键栏已有图腾，使用它
            hotbarTotemSlot = totemInHotbar;
            hotbarTotemActive = true;
            setSelectedHotbarSlot(hotbarTotemSlot);

            if (getBoolSettingValue("调试模式")) {
                System.out.println("[AutoTotem] 使用热键栏已有图腾，槽位: " + hotbarTotemSlot);
            }
            return true;
        }

        // 查找热键栏空位
        int emptyHotbarSlot = findEmptyHotbarSlot();
        if (emptyHotbarSlot == -1) {
            // 没有空位，尝试找一个非重要的槽位
            emptyHotbarSlot = findReplaceableHotbarSlot();
            if (emptyHotbarSlot == -1) {
                if (getBoolSettingValue("调试模式")) {
                    System.out.println("[AutoTotem] 热键栏没有可用的槽位");
                }
                return false;
            }
        }

        // 查找图腾（优先背包）
        int totemSlot = findTotemInInventory();
        if (totemSlot == -1) {
            if (getBoolSettingValue("调试模式")) {
                System.out.println("[AutoTotem] 没有找到图腾");
            }
            return false;
        }

        // 🆕 记录原来的选中槽位（如果是第一次设置热键栏图腾）
        if (originalSelectedSlot == -1) {
            originalSelectedSlot = getSelectedHotbarSlot();
            shouldRestoreSelectedSlot = true;
        }

        // 移动图腾到热键栏空位
        boolean success = moveTotemToHotbar(totemSlot, emptyHotbarSlot);
        if (success) {
            hotbarTotemSlot = emptyHotbarSlot;
            hotbarTotemActive = true;
            hotbarTotemCooldown = 5; // 短暂冷却
            setSelectedHotbarSlot(hotbarTotemSlot);

            if (getBoolSettingValue("调试模式")) {
                System.out.println("[AutoTotem] 图腾已移动到热键栏槽位: " + hotbarTotemSlot);
            }
        }

        return success;
    }

    /**
     * 🆕 获取当前选中的热键栏槽位
     */
    private int getSelectedHotbarSlot() {
        return inv.getSelectedHotbarIndex();
    }

    /**
     * 🆕 设置热键栏选中槽位
     */
    private void setSelectedHotbarSlot(int slot) {
        inv.setSelectedHotbarIndex(slot);
    }

    /**
     * 🆕 查找热键栏中的图腾
     */
    private int findTotemInHotbar() {
        if (mc.player == null) return -1;

        for (int slot = FullScanInventoryManager.CLIENT_HOTBAR_START;
             slot <= FullScanInventoryManager.CLIENT_HOTBAR_END; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (inv.isTotem(stack)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * 🆕 查找热键栏空位
     */
    private int findEmptyHotbarSlot() {
        if (mc.player == null) return -1;

        for (int slot = FullScanInventoryManager.CLIENT_HOTBAR_START;
             slot <= FullScanInventoryManager.CLIENT_HOTBAR_END; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * 🆕 查找可替换的热键栏槽位
     */
    private int findReplaceableHotbarSlot() {
        if (mc.player == null) return -1;

        int currentSlot = getSelectedHotbarSlot();
        int prioritySlot = (int) getDoubleSettingValue("热键栏优先级");

        // 优先选择优先级指定的槽位，如果不是很重要
        if (prioritySlot >= 0 && prioritySlot <= 8) {
            ItemStack stack = mc.player.getInventory().getStack(prioritySlot);
            if (prioritySlot != currentSlot && !isImportantItem(stack)) {
                return prioritySlot;
            }
        }

        // 从热键栏的末尾开始向前找
        for (int i = FullScanInventoryManager.CLIENT_HOTBAR_END;
             i >= FullScanInventoryManager.CLIENT_HOTBAR_START; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (i != currentSlot && !isImportantItem(stack)) {
                return i;
            }
        }

        // 如果所有槽位都是重要物品，返回当前槽位旁边的槽位
        int slot = (currentSlot + 1) % 9;
        return slot;
    }

    /**
     * 🆕 判断物品是否重要
     * 使用1.21.8的组件系统来判断
     */
    private boolean isImportantItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. 检查是否是武器（剑、斧等）
        if (stack.getDamage() > 0) {
            // 有攻击伤害的可能是武器
            return true;
        }

        // 2. 检查是否有工具组件
        ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
        if (toolComponent != null) {
            // 有工具组件的是工具
            return true;
        }

        // 3. 检查特定重要物品
        if (stack.getItem() == Items.WATER_BUCKET) return true;
        if (stack.getItem() == Items.LAVA_BUCKET) return true;
        if (stack.getItem() == Items.ELYTRA) return true;
        if (stack.getItem() == Items.BOW) return true;
        if (stack.getItem() == Items.CROSSBOW) return true;
        if (stack.getItem() == Items.TRIDENT) return true;
        if (stack.getItem() == Items.SHIELD) return true;

        // 4. 检查是否是工具类（使用物品标签或其他特征）
        // 这里使用更通用的方法：检查是否有耐久度
        if (stack.isDamageable()) {
            // 可损坏的物品通常是工具或武器
            return true;
        }

        return false;
    }

    /**
     * 🆕 查找背包中的图腾
     */
    private int findTotemInInventory() {
        if (mc.player == null) return -1;

        // 优先查找背包（非热键栏）
        for (int slot = FullScanInventoryManager.CLIENT_INVENTORY_START;
             slot <= FullScanInventoryManager.CLIENT_INVENTORY_END; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (inv.isTotem(stack)) {
                return slot;
            }
        }

        // 再查找盔甲槽
        for (int slot = FullScanInventoryManager.CLIENT_ARMOR_START;
             slot <= FullScanInventoryManager.CLIENT_ARMOR_END; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (inv.isTotem(stack)) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * 🆕 移动图腾到热键栏
     */
    private boolean moveTotemToHotbar(int fromSlot, int toHotbarSlot) {
        return inv.quickSwap(fromSlot, toHotbarSlot);
    }

    /**
     * 🆕 检查热键栏图腾状态
     */
    private void checkHotbarTotemStatus() {
        if (mc.player == null || hotbarTotemSlot == -1) return;

        ItemStack stack = mc.player.getInventory().getStack(hotbarTotemSlot);
        if (!inv.isTotem(stack)) {
            // 热键栏图腾不见了（被玩家移动/使用了）
            cleanupHotbarTotem();
        }
    }

    /**
     * 🆕 清理热键栏图腾状态
     */
    private void cleanupHotbarTotem() {
        hotbarTotemActive = false;
        hotbarTotemSlot = -1;

        // 恢复玩家原来的选中槽位
        if (shouldRestoreSelectedSlot && originalSelectedSlot != -1) {
            restoreOriginalSelectedSlot();
        }

        if (getBoolSettingValue("调试模式")) {
            System.out.println("[AutoTotem] 已清理热键栏图腾状态");
        }
    }

    /**
     * 🆕 恢复原来的选中槽位
     */
    private void restoreOriginalSelectedSlot() {
        if (originalSelectedSlot >= 0 && originalSelectedSlot <= 8) {
            setSelectedHotbarSlot(originalSelectedSlot);
            originalSelectedSlot = -1;
            shouldRestoreSelectedSlot = false;
        }
    }

    private void checkTNTMinecarts() {
        if (mc.player == null || mc.world == null) return;

        double range = getTntMinecartRangeValue();
        Vec3d playerPos = mc.player.getPos();
        Box detectionBox = new Box(
                playerPos.x - range, playerPos.y - 3, playerPos.z - range,
                playerPos.x + range, playerPos.y + 3, playerPos.z + range
        );

        List<Entity> entities = mc.world.getOtherEntities(null, detectionBox,
                entity -> entity instanceof TntMinecartEntity);

        if (!entities.isEmpty()) {
            emergencyMode = true;

            if (getBoolSettingValue("调试模式")) {
                System.out.println("[AutoTotem] 检测到TNT矿车: " + entities.size() + "个");
            }
        }
    }

    /**
     * 🆕 检查是否处于危险中
     */
    private boolean isInDanger() {
        if (mc.player == null) return false;

        // 检查生命值
        double healthThreshold = getHealthThresholdValue();
        float health = mc.player.getHealth();
        float absorption = mc.player.getAbsorptionAmount();
        float totalHealth = health + absorption;

        if (totalHealth <= healthThreshold) {
            return true;
        }

        // 检查TNT矿车
        if (getBoolSettingValue("TNT矿车检测")) {
            double range = getTntMinecartRangeValue();
            Vec3d playerPos = mc.player.getPos();
            Box detectionBox = new Box(
                    playerPos.x - range, playerPos.y - 3, playerPos.z - range,
                    playerPos.x + range, playerPos.y + 3, playerPos.z + range
            );

            List<Entity> entities = mc.world.getOtherEntities(null, detectionBox,
                    entity -> entity instanceof TntMinecartEntity);

            if (!entities.isEmpty()) {
                return true;
            }
        }

        // 检查摔落危险
        return isInFallDanger();
    }

    /**
     * 🆕 检查危险条件
     */
    private boolean checkDangerConditions() {
        if (mc.player == null) return false;

        // 1. 检查始终激活设置
        if (getBoolSettingValue("始终激活")) {
            return true;
        }

        // 2. 检查是否处于危险中
        return isInDanger();
    }

    private boolean isInFallDanger() {
        if (mc.player == null) return false;

        double fallDistance = mc.player.fallDistance;
        float health = mc.player.getHealth();
        float absorption = mc.player.getAbsorptionAmount();
        float totalHealth = health + absorption;

        if (fallDistance > 6.0) {
            double damage = (fallDistance - 3.0) * 1.0;
            return totalHealth - (float)damage <= 4.0f;
        }

        return false;
    }

    // 🎯 获取滑块值的方法
    private double getHealthThresholdValue() {
        if (healthThresholdSlider != null) {
            return healthThresholdSlider.getValue();
        }
        Setting<?> setting = getSettingByName("生命阈值");
        if (setting instanceof DoubleSetting) {
            return ((DoubleSetting) setting).getValue();
        }
        return 10.0;
    }

    private double getTntMinecartRangeValue() {
        if (tntMinecartRangeSlider != null) {
            return tntMinecartRangeSlider.getValue();
        }
        Setting<?> setting = getSettingByName("检测范围");
        if (setting instanceof DoubleSetting) {
            return ((DoubleSetting) setting).getValue();
        }
        return 12.0;
    }

    private void updateDisplayInfo() {
        boolean hasTotemInOffhand = inv.hasTotemInOffhand();

        if (hotbarTotemActive) {
            // 🆕 显示热键栏图腾状态
            setDisplayInfo("§6H"); // H表示Hotbar（热键栏）
        } else if (emergencyMode) {
            setDisplayInfo(hasTotemInOffhand ? "§c!" : "§4!!!");
        } else {
            String color = hasTotemInOffhand ? "§a" : "§c";
            setDisplayInfo(color + totemCount);
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(totemCount);
    }
}