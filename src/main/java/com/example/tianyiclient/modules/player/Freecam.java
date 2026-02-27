package com.example.tianyiclient.modules.player;

import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.modules.Category;
import com.example.tianyiclient.event.EventHandler;
import com.example.tianyiclient.event.events.client.TickEvent;
import com.example.tianyiclient.event.events.network.PacketEvent;
import com.example.tianyiclient.settings.DoubleSetting;
import com.example.tianyiclient.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {

    private final DoubleSetting speed = new DoubleSetting("移动速度", "移动速度", 0.5, 0.1, 2.0);
    private final BooleanSetting noclip = new BooleanSetting("穿墙", "穿墙", true);
    private final BooleanSetting allowInteract = new BooleanSetting("允许交互", "允许交互", true);

    private Vec3d cameraPos;
    private float cameraYaw;
    private float cameraPitch;
    private Entity originalVehicle;
    private boolean wasFlying;
    private boolean wasNoClip;
    private double lastMouseX;
    private double lastMouseY;

    public Freecam() {
        super("自由视角", "自由视角", Category.玩家);  // 注意：应该是 PLAYER 不是中文"玩家"
        addSetting(speed);
        addSetting(noclip);
        addSetting(allowInteract);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;

        // 保存原始状态
        cameraPos = mc.player.getPos();
        cameraYaw = mc.player.getYaw();
        cameraPitch = mc.player.getPitch();

        originalVehicle = mc.player.getVehicle();
        wasFlying = mc.player.getAbilities().flying;
        wasNoClip = mc.player.noClip;

        // 设置自由视角状态
        mc.player.setNoGravity(true);
        mc.player.getAbilities().flying = true;
        mc.player.noClip = noclip.getValue();

        if (originalVehicle != null) {
            mc.player.stopRiding();
        }

        // 记录鼠标位置
        lastMouseX = mc.mouse.getX();
        lastMouseY = mc.mouse.getY();
    }

    @Override
    protected void onDisable() {
        if (mc.player == null) return;

        // 恢复原始状态
        mc.player.setNoGravity(false);
        mc.player.getAbilities().flying = wasFlying;
        mc.player.noClip = wasNoClip;

        if (originalVehicle != null) {
            mc.player.startRiding(originalVehicle, true);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        // 处理鼠标移动
        double currentMouseX = mc.mouse.getX();
        double currentMouseY = mc.mouse.getY();

        double deltaX = currentMouseX - lastMouseX;
        double deltaY = currentMouseY - lastMouseY;

        cameraYaw += (float) deltaX * 0.3f;
        cameraPitch -= (float) deltaY * 0.3f;

        if (cameraPitch > 90) cameraPitch = 90;
        if (cameraPitch < -90) cameraPitch = -90;

        lastMouseX = currentMouseX;
        lastMouseY = currentMouseY;

        // 移动处理
        double spd = speed.getValue();
        if (mc.player.isSprinting()) spd *= 1.5;

        Vec3d forward = Vec3d.fromPolar(0, cameraYaw).multiply(spd);
        Vec3d right = Vec3d.fromPolar(0, cameraYaw + 90).multiply(spd);

        if (mc.options.forwardKey.isPressed()) {
            cameraPos = cameraPos.add(forward);
        }
        if (mc.options.backKey.isPressed()) {
            cameraPos = cameraPos.subtract(forward);
        }
        if (mc.options.leftKey.isPressed()) {
            cameraPos = cameraPos.subtract(right);
        }
        if (mc.options.rightKey.isPressed()) {
            cameraPos = cameraPos.add(right);
        }
        if (mc.options.jumpKey.isPressed()) {
            cameraPos = cameraPos.add(0, spd, 0);
        }
        if (mc.options.sneakKey.isPressed()) {
            cameraPos = cameraPos.subtract(0, spd, 0);
        }

        // 穿墙更新
        mc.player.noClip = noclip.getValue();

        // 重要：设置玩家的位置为摄像机位置，这样就能看到本体
        mc.player.setPosition(cameraPos);
        mc.player.setYaw(cameraYaw);
        mc.player.setPitch(cameraPitch);
    }

    @EventHandler
    public void onPacketSend(PacketEvent event) {
        if (!isEnabled()) return;

        // 取消所有移动包（让服务器认为你在原地）
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }

        // 允许交互（挖方块、攻击等）
        if (allowInteract.getValue()) {
            if (event.getPacket() instanceof PlayerActionC2SPacket ||
                    event.getPacket() instanceof PlayerInteractBlockC2SPacket ||
                    event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
                // 允许这些包通过
                return;
            }
        }
    }

    // 获取相机位置供其他模块使用
    public Vec3d getCameraPos() {
        return cameraPos;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }
}