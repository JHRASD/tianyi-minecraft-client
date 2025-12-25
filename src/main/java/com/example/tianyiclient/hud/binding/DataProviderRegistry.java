package com.example.tianyiclient.hud.binding;

import net.minecraft.client.MinecraftClient;
import java.util.function.Supplier;

/**
 * 数据提供器注册表 - 提供各种游戏数据的获取函数
 */
public class DataProviderRegistry {
    // 这里应该用静态方法获取MinecraftClient实例
    private static MinecraftClient getMC() {
        return MinecraftClient.getInstance();
    }

    // ========== 基础游戏数据 ==========
    public static final Supplier<String> FPS = () ->
            String.valueOf(getMC().getCurrentFps());

    public static final Supplier<String> COORDS = () -> {
        var player = getMC().player;
        if (player == null) return "0, 0, 0";
        return String.format("%.1f, %.1f, %.1f",
                player.getX(), player.getY(), player.getZ());
    };

    public static final Supplier<String> DIRECTION = () -> {
        var player = getMC().player;
        if (player == null) return "N/A";
        float yaw = player.getYaw();
        yaw %= 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 337.5 || yaw < 22.5) return "南";
        else if (yaw >= 22.5 && yaw < 67.5) return "西南";
        else if (yaw >= 67.5 && yaw < 112.5) return "西";
        else if (yaw >= 112.5 && yaw < 157.5) return "西北";
        else if (yaw >= 157.5 && yaw < 202.5) return "北";
        else if (yaw >= 202.5 && yaw < 247.5) return "东北";
        else if (yaw >= 247.5 && yaw < 292.5) return "东";
        else return "东南";
    };

    public static final Supplier<String> HEALTH = () -> {
        var player = getMC().player;
        if (player == null) return "0/0";
        return String.format("%.1f/%.1f",
                player.getHealth(), player.getMaxHealth());
    };

    public static final Supplier<String> HUNGER = () -> {
        var player = getMC().player;
        if (player == null) return "0";
        return String.valueOf(player.getHungerManager().getFoodLevel());
    };

    public static final Supplier<String> ARMOR = () -> {
        var player = getMC().player;
        if (player == null) return "0";
        return String.valueOf(player.getArmor());
    };

    // ========== 服务器/网络数据 ==========
    public static final Supplier<String> PING = () -> {
        var mc = getMC();
        if (mc.getNetworkHandler() == null || mc.player == null) return "N/A";
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry != null ? entry.getLatency() + "ms" : "N/A";
    };

    // ========== 世界/环境数据 ==========
    public static final Supplier<String> BIOME = () -> {
        var mc = getMC();
        if (mc.world == null || mc.player == null) return "未知";
        return mc.world.getBiome(mc.player.getBlockPos()).getKey()
                .map(key -> key.getValue().getPath().replace("_", " "))
                .orElse("未知");
    };

    public static final Supplier<String> LIGHT_LEVEL = () -> {
        var mc = getMC();
        if (mc.world == null || mc.player == null) return "0";
        return String.valueOf(mc.world.getLightLevel(mc.player.getBlockPos()));
    };

    public static final Supplier<String> GAME_TIME = () -> {
        var mc = getMC();
        if (mc.world == null) return "00:00";
        long time = mc.world.getTimeOfDay();
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    };

    // ========== 客户端数据 ==========
    public static final Supplier<String> MEMORY = () -> {
        Runtime runtime = Runtime.getRuntime();
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMB = runtime.totalMemory() / 1024 / 1024;
        return String.format("%d/%dMB", usedMB, totalMB);
    };

    public static final Supplier<String> USERNAME = () -> {
        return getMC().getSession().getUsername();
    };

    public static final Supplier<String> SERVER_IP = () -> {
        var mc = getMC();
        if (mc.getCurrentServerEntry() != null) {
            return mc.getCurrentServerEntry().address;
        } else if (mc.isInSingleplayer()) {
            return "单机游戏";
        }
        return "未知";
    };

    // ========== 辅助方法 ==========

    /**
     * 获取数据提供器的绑定（快捷方式）
     */
    public static DataBinding getBinding(String placeholder, Supplier<String> supplier) {
        return new DataBinding(placeholder, supplier::get);
    }

    /**
     * 获取预定义的绑定
     */
    public static DataBinding getFpsBinding() {
        return getBinding("${fps}", FPS);
    }

    public static DataBinding getCoordsBinding() {
        return getBinding("${coords}", COORDS);
    }

    public static DataBinding getHealthBinding() {
        return getBinding("${health}", HEALTH);
    }

    public static DataBinding getPingBinding() {
        return getBinding("${ping}", PING);
    }

    /**
     * 解析文本中的变量并自动绑定
     */
    public static String parseAndBind(String text, java.util.List<DataBinding> bindings) {
        if (text == null) return "";

        String result = text;
        for (DataBinding binding : bindings) {
            if (text.contains(binding.getPlaceholder())) {
                result = result.replace(binding.getPlaceholder(), binding.getValue());
            }
        }
        return result;
    }
}