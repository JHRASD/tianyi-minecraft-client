package com.example.tianyiclient.managers;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.event.EventBus;
import com.example.tianyiclient.event.events.network.PacketSendEvent;
import com.example.tianyiclient.event.events.network.PacketReceiveEvent;
import com.example.tianyiclient.network.PacketWrapper;
import com.example.tianyiclient.utils.PacketRotation;
import com.example.tianyiclient.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d; // 🔥 添加这个导入

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketManager extends Manager {
    private static PacketManager instance;
    private final CopyOnWriteArrayList<PacketInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final Random random = new Random();

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Silent Aim相关字段
    private Rotation silentAimRotation = null;
    private boolean silentAimEnabled = false;
    private float antiCheatOffset = 0.3f;

    // Grim服务器专用字段
    private boolean grimMode = false;
    private final List<Rotation> rotationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 15;
    private long lastAttackTime = 0;
    private boolean shouldUseSilentAimForNextAttack = false;
    private Rotation attackRotation = null;
    private Rotation forcedAttackRotation = null;

    // 调试模式
    private boolean debugMode = false;

    // 控制日志频率
    private long lastPacketLogTime = 0;
    private long lastAngleLogTime = 0;
    private static final long LOG_INTERVAL = 500;

    private PacketManager() {
        super("PacketManager");
        TianyiClient.LOGGER.info("PacketManager 初始化");
    }

    public static PacketManager getInstance() {
        if (instance == null) {
            instance = new PacketManager();
        }
        return instance;
    }

    @Override
    public void onInit() {
        TianyiClient.LOGGER.info("PacketManager 初始化完成");
        PacketRotation.setDebugMode(debugMode);
    }

    public void prepareRotationForAttack(Entity target) {
        if (target == null || mc.player == null || mc.getNetworkHandler() == null) return;

        // 获取玩家眼睛位置
        Vec3d eyePos = mc.player.getEyePos();
        // 获取实体中心位置
        Vec3d targetPos = target.getBoundingBox().getCenter();
        // 计算差值
        Vec3d diff = targetPos.subtract(eyePos);
        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        double yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double pitch = Math.toDegrees(Math.atan2(-diff.y, horizontalDistance));

        forcedAttackRotation = new Rotation((float) yaw, (float) pitch);
        setSilentAimRotation(forcedAttackRotation);

        try {
            PlayerMoveC2SPacket lookPacket = new PlayerMoveC2SPacket.LookAndOnGround(
                    (float) yaw,
                    (float) pitch,
                    mc.player.isOnGround(),
                    false
            );
            mc.getNetworkHandler().sendPacket(lookPacket);

            shouldUseSilentAimForNextAttack = true;
            attackRotation = forcedAttackRotation;

            if (debugMode) {
                System.out.println("[PacketManager] 🔥 已为攻击预置角度并发送包: " + forcedAttackRotation);
            }
        } catch (Exception e) {
            if (debugMode) {
                System.err.println("[PacketManager] ❌ 发送攻击前旋转包失败: " + e.getMessage());
            }
        }
    }

    public boolean handlePacketSend(Packet<?> packet) {
        if (!enabled.get()) return true;

        Packet<?> packetToSend = packet;

        Rotation rotationToUse = null;
        if (shouldUseSilentAimForNextAttack && attackRotation != null) {
            rotationToUse = attackRotation;
        } else if (silentAimEnabled && silentAimRotation != null) {
            rotationToUse = silentAimRotation;
        }

        if (packet instanceof PlayerInteractEntityC2SPacket) {
            shouldUseSilentAimForNextAttack = true;
            attackRotation = silentAimRotation;

            if (debugMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPacketLogTime > LOG_INTERVAL) {
                    System.out.println("[PacketManager] 🔥 检测到攻击包: PlayerInteractEntityC2SPacket");
                    lastPacketLogTime = currentTime;
                }
            }
        }

        if (packet instanceof PlayerInteractEntityC2SPacket) {
            shouldUseSilentAimForNextAttack = false;
            attackRotation = null;
            forcedAttackRotation = null;
        }

        if (rotationToUse != null && packet instanceof PlayerMoveC2SPacket) {
            if (grimMode) {
                packetToSend = processPacketForGrim((PlayerMoveC2SPacket) packet, rotationToUse); // 🔥 强制类型转换
            } else {
                packetToSend = processSilentAim(packet, rotationToUse);
            }

            if (shouldUseSilentAimForNextAttack) {
                shouldUseSilentAimForNextAttack = false;
            }
        }

        PacketWrapper wrapper = new PacketWrapper(packetToSend, PacketWrapper.Direction.SEND);
        PacketSendEvent event = new PacketSendEvent(wrapper);

        EventBus.getInstance().post(event);

        for (PacketInterceptor interceptor : interceptors) {
            if (!interceptor.onPacketSend(wrapper)) {
                return false;
            }
        }

        if (event.isCancelled() || wrapper.isCancelled()) {
            if (debugMode) {
                TianyiClient.LOGGER.debug("[PacketSystem] 取消发送包: {}", wrapper.getPacketName());
            }
            return false;
        }

        return true;
    }

    public boolean handlePacketReceive(Packet<?> packet) {
        if (!enabled.get()) return true;

        PacketWrapper wrapper = new PacketWrapper(packet, PacketWrapper.Direction.RECEIVE);
        PacketReceiveEvent event = new PacketReceiveEvent(wrapper);

        EventBus.getInstance().post(event);

        for (PacketInterceptor interceptor : interceptors) {
            if (!interceptor.onPacketReceive(wrapper)) {
                return false;
            }
        }

        if (event.isCancelled() || wrapper.isCancelled()) {
            if (debugMode) {
                TianyiClient.LOGGER.debug("[PacketSystem] 取消接收包: {}", wrapper.getPacketName());
            }
            return false;
        }

        return true;
    }

    private Packet<?> processSilentAim(Packet<?> packet, Rotation rotation) {
        if (!PacketRotation.isRotationPacket(packet)) {
            return packet;
        }

        try {
            Rotation offsetRotation = addAntiCheatOffset(rotation);
            Packet<?> modifiedPacket = PacketRotation.modifyPacketRotation(packet, offsetRotation);

            if (debugMode && modifiedPacket != packet) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPacketLogTime > LOG_INTERVAL) {
                    System.out.println("[PacketManager] ✅ 旋转包修改成功");
                    lastPacketLogTime = currentTime;
                }
            }
            return modifiedPacket;
        } catch (Exception e) {
            if (debugMode) {
                System.err.println("[SilentAim] ❌ 修改失败: " + e.getMessage());
            }
            return packet;
        }
    }

    // 🔥 修复：明确指定参数类型
    private Packet<?> processPacketForGrim(PlayerMoveC2SPacket packet, Rotation rotation) {
        if (!grimMode) return packet;

        return processMovementForGrim(packet, rotation);
    }

    // 🔥 修复：明确指定参数类型
    private PlayerMoveC2SPacket processMovementForGrim(PlayerMoveC2SPacket packet, Rotation rotation) {
        if (!packet.changesLook()) {
            return packet;
        }

        if (!checkRotationHistory(rotation)) {
            return packet;
        }

        rotationHistory.add(rotation);
        if (rotationHistory.size() > MAX_HISTORY) {
            rotationHistory.remove(0);
        }

        Rotation finalRotation = addGrimOffset(rotation);

        try {
            PlayerMoveC2SPacket modified = (PlayerMoveC2SPacket) PacketRotation.modifyPacketRotation(packet, finalRotation);

            if (debugMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPacketLogTime > LOG_INTERVAL) {
                    System.out.println("[GrimPacket] ✅ 包已修改");
                    System.out.println("[GrimPacket]   原角度: " + packet.getYaw(0) + ", " + packet.getPitch(0));
                    System.out.println("[GrimPacket]   新角度: " + finalRotation.getYaw() + ", " + finalRotation.getPitch());
                    lastPacketLogTime = currentTime;
                }
            }

            return modified;
        } catch (Exception e) {
            return packet;
        }
    }

    private boolean checkRotationHistory(Rotation newRotation) {
        if (rotationHistory.isEmpty()) return true;

        Rotation last = rotationHistory.get(rotationHistory.size() - 1);

        float yawDiff = Math.abs(newRotation.getYaw() - last.getYaw());
        float pitchDiff = Math.abs(newRotation.getPitch() - last.getPitch());

        while (yawDiff > 180) yawDiff = 360 - yawDiff;

        if (yawDiff > 25 || pitchDiff > 25) {
            if (debugMode) {
                System.err.println("[GrimPacket] ⚠ 角度变化过大: " + yawDiff + ", " + pitchDiff);
            }
            return false;
        }

        return true;
    }

    private Rotation addGrimOffset(Rotation rotation) {
        if (rotation == null) return null;

        float baseYawOffset = 0.0f;
        float basePitchOffset = 0.0f;

        if (rotationHistory.size() >= 5) {
            long sameCount = rotationHistory.stream()
                    .filter(r -> Math.abs(r.getYaw() - rotation.getYaw()) < 0.1f)
                    .count();

            if (sameCount > 3) {
                baseYawOffset = (random.nextFloat() - 0.5f) * 1.5f;
            }
        }

        float yawOffset = baseYawOffset + (random.nextFloat() - 0.5f) * 0.25f;
        float pitchOffset = basePitchOffset + (random.nextFloat() - 0.5f) * 0.15f;

        return new Rotation(rotation.getYaw() + yawOffset, rotation.getPitch() + pitchOffset);
    }

    private Rotation addAntiCheatOffset(Rotation rotation) {
        if (rotation == null) return null;

        float yawOffset = (random.nextFloat() - 0.5f) * 0.2f * antiCheatOffset;
        float pitchOffset = (random.nextFloat() - 0.5f) * 0.2f * antiCheatOffset;

        return new Rotation(rotation.getYaw() + yawOffset, rotation.getPitch() + pitchOffset);
    }

    public void injectPacket(Packet<?> packet, PacketWrapper.Direction direction) {
        if (debugMode) {
            TianyiClient.LOGGER.info("[PacketSystem] 注入数据包: {} 方向: {}",
                    packet.getClass().getSimpleName(), direction);
        }
    }

    public void registerInterceptor(PacketInterceptor interceptor) {
        interceptors.add(interceptor);
        if (debugMode) {
            TianyiClient.LOGGER.debug("[PacketSystem] 注册包拦截器: {}", interceptor.getClass().getSimpleName());
        }
    }

    public void unregisterInterceptor(PacketInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    public void setSilentAimRotation(Rotation rotation) {
        this.silentAimRotation = rotation;

        if (rotation != null) {
            this.silentAimEnabled = true;

            if (debugMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAngleLogTime > LOG_INTERVAL) {
                    System.out.println("[PacketManager] ✅ Silent Aim: 角度已设置");
                    System.out.println("[PacketManager]   角度: yaw=" + rotation.getYaw() + ", pitch=" + rotation.getPitch());
                    lastAngleLogTime = currentTime;
                }
            }
        } else {
            if (debugMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAngleLogTime > LOG_INTERVAL) {
                    System.out.println("[PacketManager] ℹ Silent Aim: 角度已清空");
                    lastAngleLogTime = currentTime;
                }
            }
        }
    }

    public Rotation getSilentAimRotation() {
        return silentAimRotation;
    }

    public void setSilentAimEnabled(boolean enabled) {
        this.silentAimEnabled = enabled;

        if (debugMode) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAngleLogTime > LOG_INTERVAL) {
                System.out.println("[PacketManager] " + (enabled ? "✅" : "❌") + " Silent Aim " + (enabled ? "启用" : "禁用"));
                lastAngleLogTime = currentTime;
            }
        }
    }

    public boolean isSilentAimEnabled() {
        return silentAimEnabled;
    }

    public void setGrimMode(boolean enabled) {
        this.grimMode = enabled;

        if (enabled) {
            rotationHistory.clear();
            if (debugMode) {
                System.out.println("[PacketManager] 🔒 Grim模式已启用");
            }
        }
    }

    public void forceFixSilentAim() {
        if (debugMode) {
            System.out.println("[PacketManager] 🔧 强制修复Silent Aim");
        }
        this.silentAimEnabled = true;
    }

    public void setAntiCheatOffset(float offset) {
        this.antiCheatOffset = offset;
    }

    public float getAntiCheatOffset() {
        return antiCheatOffset;
    }

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        PacketRotation.setDebugMode(enabled);
        if (debugMode) {
            System.out.println("[PacketManager] Debug mode: " + enabled);
        }
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public void onShutdown() {
        TianyiClient.LOGGER.info("PacketManager 关闭");
        interceptors.clear();
        enabled.set(false);

        silentAimEnabled = false;
        silentAimRotation = null;
        attackRotation = null;
        forcedAttackRotation = null;
        shouldUseSilentAimForNextAttack = false;
        rotationHistory.clear();
    }

    public interface PacketInterceptor {
        default boolean onPacketSend(PacketWrapper packet) { return true; }
        default boolean onPacketReceive(PacketWrapper packet) { return true; }
    }
}