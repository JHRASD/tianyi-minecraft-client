package com.example.tianyiclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 修复版物品栏操作工具类 - 适配Minecraft 1.21.8
 * 包含热键栏操作方法（修复私有字段访问问题）
 */
public class FullScanInventoryManager {

    private static FullScanInventoryManager instance;
    private final MinecraftClient mc;

    // ========== 服务器端槽位索引（用于 clickSlot 操作）==========
    public static final int SERVER_OFFHAND_SLOT = 45;
    public static final int SERVER_HELMET_SLOT = 5;
    public static final int SERVER_CHESTPLATE_SLOT = 6;
    public static final int SERVER_LEGGINGS_SLOT = 7;
    public static final int SERVER_BOOTS_SLOT = 8;
    public static final int SERVER_HOTBAR_START = 36;
    public static final int SERVER_HOTBAR_END = 44;
    public static final int SERVER_INVENTORY_START = 9;
    public static final int SERVER_INVENTORY_END = 35;
    public static final int SERVER_ARMOR_START = 5;
    public static final int SERVER_ARMOR_END = 8;

    // ========== 客户端槽位索引（用于 getStack 查询）==========
    public static final int CLIENT_HOTBAR_START = 0;
    public static final int CLIENT_HOTBAR_END = 8;
    public static final int CLIENT_INVENTORY_START = 9;
    public static final int CLIENT_INVENTORY_END = 35;
    public static final int CLIENT_ARMOR_START = 36;
    public static final int CLIENT_ARMOR_END = 39;
    public static final int CLIENT_OFFHAND = 40;
    public static final int CLIENT_CRAFTING_OUTPUT = 0;

    // 性能控制
    private boolean ultraFastMode = true;
    private long lastOperationTime = 0;
    private static final long MIN_OPERATION_INTERVAL = 1;
    private boolean debugMode = false;

    private FullScanInventoryManager() {
        this.mc = MinecraftClient.getInstance();
    }

    public static FullScanInventoryManager getInstance() {
        if (instance == null) {
            instance = new FullScanInventoryManager();
        }
        return instance;
    }

    // ==================== 新增：热键栏操作方法 ====================

    /**
     * 🆕 获取当前选中的热键栏槽位（0-8）
     * 正确方法：使用PlayerInventory的getSelectedSlot()方法（如果存在）
     * 或者直接访问字段（在Minecraft 1.21.8中可能是公开的）
     */
    public int getSelectedHotbarIndex() {
        if (mc.player == null) return 0;

        try {
            // 方法1：尝试使用getter方法（如果存在）
            java.lang.reflect.Method method = PlayerInventory.class.getMethod("getSelectedSlot");
            return (int) method.invoke(mc.player.getInventory());
        } catch (NoSuchMethodException e1) {
            try {
                // 方法2：尝试直接访问字段（使用反射）
                java.lang.reflect.Field field = PlayerInventory.class.getDeclaredField("selectedSlot");
                field.setAccessible(true);
                return (int) field.get(mc.player.getInventory());
            } catch (Exception e2) {
                // 方法3：备用方案，使用输入管理器
                debug("无法获取选中槽位，使用默认值0");
                return 0;
            }
        } catch (Exception e) {
            debug("获取选中槽位失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 🆕 设置热键栏选中槽位（0-8）
     * 正确方法：使用PlayerInventory的setSelectedSlot()方法（如果存在）
     */
    public void setSelectedHotbarIndex(int index) {
        if (mc.player == null || index < 0 || index > 8) return;

        try {
            // 方法1：尝试使用setter方法（如果存在）
            java.lang.reflect.Method method = PlayerInventory.class.getMethod("setSelectedSlot", int.class);
            method.invoke(mc.player.getInventory(), index);
            debug("设置选中槽位: " + index);
            return;
        } catch (NoSuchMethodException e1) {
            try {
                // 方法2：尝试直接设置字段（使用反射）
                java.lang.reflect.Field field = PlayerInventory.class.getDeclaredField("selectedSlot");
                field.setAccessible(true);
                field.set(mc.player.getInventory(), index);
                debug("通过反射设置选中槽位: " + index);
                return;
            } catch (Exception e2) {
                debug("反射设置选中槽位失败: " + e2.getMessage());
            }
        } catch (Exception e) {
            debug("设置选中槽位失败: " + e.getMessage());
        }

        // 方法3：通过输入模拟切换槽位（最可靠）
        simulateHotbarSelection(index);
    }

    /**
     * 🆕 通过模拟输入来切换热键栏槽位
     * 这是最可靠的方法，因为Minecraft会处理所有相关的逻辑
     */
    private void simulateHotbarSelection(int slot) {
        if (mc.player == null || slot < 0 || slot > 8) return;

        // 存储当前选中槽位
        int currentSlot = getSelectedHotbarIndex();

        if (currentSlot == slot) {
            // 已经在目标槽位，不需要切换
            return;
        }

        // 模拟按数字键切换到目标槽位
        // 注意：需要处理Minecraft的输入系统
        // 这里使用一个简单的实现：直接设置槽位并通知客户端

        try {
            // 通过客户端玩家网络处理器发送数据包
            if (mc.player.networkHandler != null) {
                // 创建切换槽位的数据包
                net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket packet =
                        new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot);
                mc.player.networkHandler.sendPacket(packet);
                debug("发送切换槽位数据包: " + slot);
            }
        } catch (Exception e) {
            debug("发送切换槽位数据包失败: " + e.getMessage());
        }
    }

    /**
     * 🆕 切换热键栏槽位
     */
    public void switchToHotbarSlot(int slot) {
        setSelectedHotbarIndex(slot);
    }

    /**
     * 🆕 切换到下一个热键栏槽位
     */
    public void switchToNextHotbarSlot() {
        int current = getSelectedHotbarIndex();
        setSelectedHotbarIndex((current + 1) % 9);
    }

    /**
     * 🆕 切换到上一个热键栏槽位
     */
    public void switchToPreviousHotbarSlot() {
        int current = getSelectedHotbarIndex();
        setSelectedHotbarIndex((current + 8) % 9); // +8 等价于 -1，但确保正数
    }

    /**
     * 🆕 获取热键栏指定槽位的物品
     */
    public ItemStack getHotbarItem(int slot) {
        if (mc.player == null || slot < 0 || slot > 8) return ItemStack.EMPTY;
        return mc.player.getInventory().getStack(slot);
    }

    /**
     * 🆕 检查热键栏指定槽位是否有物品
     */
    public boolean isHotbarSlotEmpty(int slot) {
        return getHotbarItem(slot).isEmpty();
    }

    /**
     * 🆕 检查热键栏是否有空位
     */
    public boolean hasEmptyHotbarSlot() {
        for (int slot = CLIENT_HOTBAR_START; slot <= CLIENT_HOTBAR_END; slot++) {
            if (isHotbarSlotEmpty(slot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 🆕 查找第一个空的热键栏槽位
     */
    public int findEmptyHotbarSlot() {
        for (int slot = CLIENT_HOTBAR_START; slot <= CLIENT_HOTBAR_END; slot++) {
            if (isHotbarSlotEmpty(slot)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * 🆕 查找最佳的热键栏槽位（优先空位，然后是当前槽位）
     */
    public int findBestHotbarSlotForTotem() {
        // 1. 优先查找空槽位
        int emptySlot = findEmptyHotbarSlot();
        if (emptySlot != -1) {
            return emptySlot;
        }

        // 2. 使用当前选中槽位
        int currentSlot = getSelectedHotbarIndex();
        return currentSlot;
    }

    // ==================== 索引转换方法 ====================

    private int toServerSlot(int clientSlot) {
        if (clientSlot >= CLIENT_HOTBAR_START && clientSlot <= CLIENT_HOTBAR_END) {
            // 热键栏 0-8 → 36-44
            return SERVER_HOTBAR_START + clientSlot;
        } else if (clientSlot >= CLIENT_INVENTORY_START && clientSlot <= CLIENT_INVENTORY_END) {
            // 背包 9-35 → 9-35 (保持不变)
            return clientSlot;
        } else if (clientSlot >= CLIENT_ARMOR_START && clientSlot <= CLIENT_ARMOR_END) {
            // 盔甲 36-39 → 5-8
            return SERVER_ARMOR_START + (clientSlot - CLIENT_ARMOR_START);
        } else if (clientSlot == CLIENT_OFFHAND) {
            // 副手 40 → 45
            return SERVER_OFFHAND_SLOT;
        } else if (clientSlot == CLIENT_CRAFTING_OUTPUT) {
            // 合成输出 0 → 0
            return 0;
        }
        return clientSlot; // 其他情况
    }

    private int toClientSlot(int serverSlot) {
        if (serverSlot >= SERVER_HOTBAR_START && serverSlot <= SERVER_HOTBAR_END) {
            // 热键栏 36-44 → 0-8
            return serverSlot - SERVER_HOTBAR_START;
        } else if (serverSlot >= SERVER_INVENTORY_START && serverSlot <= SERVER_INVENTORY_END) {
            // 背包 9-35 → 9-35 (保持不变)
            return serverSlot;
        } else if (serverSlot >= SERVER_ARMOR_START && serverSlot <= SERVER_ARMOR_END) {
            // 盔甲 5-8 → 36-39
            return CLIENT_ARMOR_START + (serverSlot - SERVER_ARMOR_START);
        } else if (serverSlot == SERVER_OFFHAND_SLOT) {
            // 副手 45 → 40
            return CLIENT_OFFHAND;
        }
        return serverSlot; // 其他情况
    }

    // ==================== 调试和控制方法 ====================

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }

    public void enableUltraFastMode(boolean enable) {
        this.ultraFastMode = enable;
    }

    private void debug(String message) {
        if (debugMode) {
            System.out.println("[InventoryManager] " + message);
        }
    }

    // ==================== 物品查找方法 ====================

    public int findItem(Item item, int minCount) {
        if (mc.player == null) return -1;

        PlayerInventory inventory = mc.player.getInventory();

        // 1. 检查副手（客户端索引40）
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.getItem() == item && offhand.getCount() >= minCount) {
            return CLIENT_OFFHAND;
        }

        // 2. 检查热键栏（客户端索引0-8）
        for (int slot = CLIENT_HOTBAR_START; slot <= CLIENT_HOTBAR_END; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() >= minCount) {
                return slot;
            }
        }

        // 3. 检查背包（客户端索引9-35）
        for (int slot = CLIENT_INVENTORY_START; slot <= CLIENT_INVENTORY_END; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() >= minCount) {
                return slot;
            }
        }

        // 4. 检查盔甲槽（客户端索引36-39）
        for (int slot = CLIENT_ARMOR_START; slot <= CLIENT_ARMOR_END; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() >= minCount) {
                return slot;
            }
        }

        return -1;
    }

    public int findItemInHotbar(Item item, int minCount) {
        if (mc.player == null) return -1;

        PlayerInventory inventory = mc.player.getInventory();
        for (int slot = CLIENT_HOTBAR_START; slot <= CLIENT_HOTBAR_END; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() >= minCount) {
                return slot;
            }
        }
        return -1;
    }

    public int countItems(Item item) {
        if (mc.player == null) return 0;

        int total = 0;
        PlayerInventory inventory = mc.player.getInventory();

        // 统计所有槽位（0-44）
        for (int slot = 0; slot <= 44; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                total += stack.getCount();
            }
        }

        // 统计副手
        ItemStack offhand = mc.player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.getItem() == item) {
            total += offhand.getCount();
        }

        return total;
    }

    // ==================== 交换方法 ====================

    public boolean quickSwap(int fromClientSlot, int toClientSlot) {
        if (!canPerformOperation()) return false;

        try {
            if (mc.interactionManager == null || mc.player == null) {
                return false;
            }

            if (fromClientSlot == toClientSlot) return false;

            debug("交换: 客户端索引[" + fromClientSlot + "] → [" + toClientSlot + "]");

            // 转换为服务器端索引
            int fromServerSlot = toServerSlot(fromClientSlot);
            int toServerSlot = toServerSlot(toClientSlot);

            debug("服务器索引: " + fromServerSlot + " → " + toServerSlot);

            int syncId = mc.player.currentScreenHandler != null ?
                    mc.player.currentScreenHandler.syncId : 0;

            // 第一步：拿起物品
            mc.interactionManager.clickSlot(syncId, fromServerSlot, 0, SlotActionType.PICKUP, mc.player);

            // 第二步：放下到目标槽位
            mc.interactionManager.clickSlot(syncId, toServerSlot, 0, SlotActionType.PICKUP, mc.player);

            lastOperationTime = System.currentTimeMillis();
            return true;

        } catch (Exception e) {
            debug("交换异常: " + e.getMessage());
            return false;
        }
    }

    public boolean safeSwap(int fromClientSlot, int toClientSlot) {
        if (!canPerformOperation()) return false;

        try {
            if (mc.interactionManager == null || mc.player == null) {
                return false;
            }

            if (fromClientSlot == toClientSlot) return false;

            debug("安全交换: " + fromClientSlot + " → " + toClientSlot);

            int syncId = mc.player.currentScreenHandler != null ?
                    mc.player.currentScreenHandler.syncId : 0;

            int fromServerSlot = toServerSlot(fromClientSlot);
            int toServerSlot = toServerSlot(toClientSlot);

            // 三步交换法（最可靠）
            mc.interactionManager.clickSlot(syncId, fromServerSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, toServerSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, fromServerSlot, 0, SlotActionType.PICKUP, mc.player);

            lastOperationTime = System.currentTimeMillis();
            return true;

        } catch (Exception e) {
            debug("安全交换异常: " + e.getMessage());
            return false;
        }
    }

    // ==================== 图腾相关方法 ====================

    public boolean equipTotemToOffhand() {
        if (mc.player == null) {
            debug("玩家为空");
            return false;
        }

        // 1. 检查副手是否已有图腾
        ItemStack offhand = mc.player.getOffHandStack();
        if (isTotem(offhand)) {
            debug("副手已有图腾");
            return true;
        }

        // 2. 优先查找热键栏（客户端索引0-8）
        int totemSlot = findItemInHotbar(Items.TOTEM_OF_UNDYING, 1);
        if (totemSlot == -1) {
            debug("热键栏未找到图腾");
            // 3. 查找整个物品栏
            totemSlot = findItem(Items.TOTEM_OF_UNDYING, 1);
        }

        if (totemSlot == -1) {
            debug("物品栏未找到图腾");
            return false;
        }

        debug("找到图腾在客户端索引: " + totemSlot);

        // 4. 交换到副手（使用正确的索引转换）
        return quickSwap(totemSlot, CLIENT_OFFHAND);
    }

    /**
     * 🆕 智能装备图腾到热键栏
     */
    public boolean equipTotemToHotbar() {
        if (mc.player == null) return false;

        // 检查热键栏是否已有图腾
        int totemInHotbar = findItemInHotbar(Items.TOTEM_OF_UNDYING, 1);
        if (totemInHotbar != -1) {
            // 已有图腾，切换到那个槽位
            setSelectedHotbarIndex(totemInHotbar);
            return true;
        }

        // 查找热键栏空位
        int emptySlot = findBestHotbarSlotForTotem();

        // 查找图腾
        int totemSlot = findItem(Items.TOTEM_OF_UNDYING, 1);
        if (totemSlot == -1) return false;

        // 移动图腾到热键栏
        boolean success = quickSwap(totemSlot, emptySlot);
        if (success) {
            setSelectedHotbarIndex(emptySlot);
        }
        return success;
    }

    // ==================== 辅助方法 ====================

    public boolean isTotem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    public boolean isElytra(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.ELYTRA;
    }

    public boolean isChestplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.LEATHER_CHESTPLATE ||
                item == Items.CHAINMAIL_CHESTPLATE;
    }

    public ItemStack getOffhandItem() {
        return mc.player != null ? mc.player.getOffHandStack().copy() : ItemStack.EMPTY;
    }

    public ItemStack getMainHandItem() {
        return mc.player != null ? mc.player.getMainHandStack().copy() : ItemStack.EMPTY;
    }

    public boolean hasTotemInOffhand() {
        if (mc.player == null) return false;
        ItemStack offhand = mc.player.getOffHandStack();
        return isTotem(offhand);
    }

    public int countAllTotems() {
        return countItems(Items.TOTEM_OF_UNDYING);
    }

    private boolean canPerformOperation() {
        if (mc.player == null || mc.interactionManager == null) {
            return false;
        }

        // 极速模式跳过冷却检查
        if (!ultraFastMode) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastOperationTime < MIN_OPERATION_INTERVAL) {
                return false;
            }
        }

        // 检查游戏状态
        if (mc.player.isDead() || mc.player.isSleeping()) {
            return false;
        }

        if (mc.currentScreen != null) {
            return false;
        }

        return true;
    }

    // ==================== 新增：实用方法 ====================

    /**
     * 🆕 获取热键栏所有槽位的物品状态
     */
    public ItemStack[] getHotbarContents() {
        ItemStack[] contents = new ItemStack[9];
        if (mc.player == null) return contents;

        for (int i = 0; i < 9; i++) {
            contents[i] = mc.player.getInventory().getStack(i).copy();
        }
        return contents;
    }

    /**
     * 🆕 检查热键栏是否有特定物品
     */
    public boolean hasItemInHotbar(Item item) {
        return findItemInHotbar(item, 1) != -1;
    }

    /**
     * 🆕 获取当前主手物品
     */
    public ItemStack getCurrentSelectedItem() {
        if (mc.player == null) return ItemStack.EMPTY;
        int selectedSlot = getSelectedHotbarIndex();
        return getHotbarItem(selectedSlot);
    }

    /**
     * 🆕 检查当前是否拿着图腾
     */
    public boolean isHoldingTotem() {
        ItemStack mainHand = getMainHandItem();
        return isTotem(mainHand);
    }

    /**
     * 🆕 检查热键栏是否已有图腾
     */
    public boolean hasTotemInHotbar() {
        return findItemInHotbar(Items.TOTEM_OF_UNDYING, 1) != -1;
    }

    /**
     * 🆕 获取热键栏中的图腾槽位
     */
    public int getTotemHotbarSlot() {
        return findItemInHotbar(Items.TOTEM_OF_UNDYING, 1);
    }

// ==================== 中键物品相关方法 ====================

    /**
     * 🆕 中键物品快速使用功能
     * 查找物品并切换到热键栏或使用
     */
    public boolean quickUseItem(Item item, boolean useImmediately, boolean returnToOriginal) {
        if (mc.player == null) return false;

        // 保存原始槽位
        int originalSlot = getSelectedHotbarIndex();
        debug("中键物品: 原始槽位=" + originalSlot + ", 目标物品=" + item.getName().getString());

        // 查找物品
        int itemSlot = findItem(item, 1);
        if (itemSlot == -1) {
            debug("未找到物品: " + item.getName().getString());
            return false;
        }

        debug("找到物品在槽位: " + itemSlot);

        // 检查是否在热键栏
        boolean inHotbar = (itemSlot >= CLIENT_HOTBAR_START && itemSlot <= CLIENT_HOTBAR_END);

        if (inHotbar) {
            // 在热键栏，直接切换
            debug("物品在热键栏，切换到槽位: " + itemSlot);
            setSelectedHotbarIndex(itemSlot);

            if (useImmediately) {
                // 立即使用
                debug("立即使用物品");
                useItem();
            }

            if (returnToOriginal) {
                // 延迟后返回原始槽位
                new Thread(() -> {
                    try {
                        Thread.sleep(200); // 200ms后返回
                        if (mc.player != null) {
                            setSelectedHotbarIndex(originalSlot);
                            debug("返回原始槽位: " + originalSlot);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            return true;
        } else {
            // 在背包，需要移动到热键栏
            debug("物品在背包，需要移动到热键栏");
            int hotbarSlot = findEmptyHotbarSlot();
            if (hotbarSlot == -1) {
                debug("热键栏没有空位，使用当前槽位");
                hotbarSlot = getSelectedHotbarIndex();
            }

            debug("移动物品: " + itemSlot + " -> " + hotbarSlot);
            boolean success = quickSwap(itemSlot, hotbarSlot);

            if (success) {
                setSelectedHotbarIndex(hotbarSlot);

                if (useImmediately) {
                    debug("立即使用物品");
                    useItem();
                }

                if (returnToOriginal) {
                    // 延迟后返回原始槽位
                    new Thread(() -> {
                        try {
                            Thread.sleep(200); // 200ms后返回
                            if (mc.player != null) {
                                setSelectedHotbarIndex(originalSlot);
                                debug("返回原始槽位: " + originalSlot);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

                return true;
            }

            return false;
        }
    }

    /**
     * 🆕 使用当前主手物品
     */
    private void useItem() {
        if (mc.interactionManager == null || mc.player == null) return;

        try {
            // 在主线程中执行物品使用
            mc.execute(() -> {
                try {
                    mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
                } catch (Exception e) {
                    debug("使用物品失败: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            debug("调度物品使用失败: " + e.getMessage());
        }
    }

    /**
     * 🆕 查找物品在热键栏中的最佳位置
     */
    public int findOptimalHotbarSlotForItem(Item item) {
        if (mc.player == null) return -1;

        // 1. 检查当前槽位
        int currentSlot = getSelectedHotbarIndex();
        ItemStack currentStack = mc.player.getInventory().getStack(currentSlot);
        if (currentStack.isEmpty()) {
            return currentSlot; // 当前槽位为空
        }

        // 2. 查找空槽位
        int emptySlot = findEmptyHotbarSlot();
        if (emptySlot != -1) {
            return emptySlot;
        }

        // 3. 查找非重要物品的槽位
        for (int slot = CLIENT_HOTBAR_START; slot <= CLIENT_HOTBAR_END; slot++) {
            if (slot == currentSlot) continue;

            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (!isImportantTool(stack)) {
                return slot;
            }
        }

        // 4. 使用当前槽位
        return currentSlot;
    }

    /**
     * 🆕 判断是否为重要工具
     */
    private boolean isImportantTool(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();

        // 重要工具列表
        String itemName = item.getName().getString().toLowerCase();

        // 检查常见重要物品
        return itemName.contains("sword") ||
                itemName.contains("pickaxe") ||
                itemName.contains("axe") ||
                itemName.contains("shovel") ||
                itemName.contains("hoe") ||
                item == Items.BOW ||
                item == Items.CROSSBOW ||
                item == Items.TRIDENT ||
                item == Items.WATER_BUCKET ||
                item == Items.LAVA_BUCKET ||
                item == Items.SHIELD ||
                item == Items.ELYTRA;
    }
}