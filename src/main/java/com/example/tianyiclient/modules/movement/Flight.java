package com.example.tianyiclient.modules.movement;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.settings.BoolSetting;
import com.example.tianyiclient.settings.DoubleSetting;
import com.example.tianyiclient.settings.IntegerSetting;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Flight extends Module {

    private BoolSetting keepFlyingSetting;
    private DoubleSetting speedSetting;
    private BoolSetting antiKickSetting;
    private IntegerSetting antiKickInterval;

    public Flight() {
        super("飞行", "允许玩家飞行", Category.移动);

        // 注意：现在不要在这里调用 initModule()
        // 设置应该在 init() 方法中添加
    }

    @Override
    protected void init() {
        // 设置默认快捷键
        setKeybind(GLFW.GLFW_KEY_V);

        // 添加布尔设置：是否保持飞行
        keepFlyingSetting = addSetting(new BoolSetting(
                "保持飞行",
                "是否在 Tick 中持续保持飞行状态",
                true
        ));

        // 添加速度设置
        speedSetting = addSetting(new DoubleSetting(
                "速度",
                "飞行速度",
                1.0,  // 默认值
                0.1,  // 最小值
                5.0   // 最大值
        ));

        // 防踢出设置
        antiKickSetting = addSetting(new BoolSetting(
                "防踢出",
                "防止服务器踢出",
                true
        ));

        // 防踢出间隔
        antiKickInterval = addSetting(new IntegerSetting(
                "防踢间隔",
                "防踢出操作的间隔(刻)",
                20,   // 默认值
                1,    // 最小值
                100   // 最大值
        ));
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed((float) getSpeedSettingValue() * 0.05f);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled() || mc.player == null) return;

        // 保持飞行状态
        if (getKeepFlyingSettingValue()) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
        }

        // 更新飞行速度
        mc.player.getAbilities().setFlySpeed((float) getSpeedSettingValue() * 0.05f);

        // 防踢出逻辑
        if (getAntiKickSettingValue() && mc.player.age % getAntiKickIntervalValue() == 0) {
            // 轻微移动防止被踢
            if (mc.player.isOnGround()) {
                mc.player.setPosition(mc.player.getX(), mc.player.getY() + 0.04, mc.player.getZ());
            }
        }
    }

    // Helper methods to safely get setting values
    private boolean getKeepFlyingSettingValue() {
        return keepFlyingSetting != null ? keepFlyingSetting.getValue() : true;
    }

    private double getSpeedSettingValue() {
        return speedSetting != null ? speedSetting.getValue() : 1.0;
    }

    private boolean getAntiKickSettingValue() {
        return antiKickSetting != null ? antiKickSetting.getValue() : true;
    }

    private int getAntiKickIntervalValue() {
        return antiKickInterval != null ? antiKickInterval.getValue() : 20;
    }

    @Override
    public String getInfo() {
        // 在HUD上显示当前速度
        setDisplayInfo(String.format("§a%.1f", getSpeedSettingValue()));
        return super.getInfo();
    }
}