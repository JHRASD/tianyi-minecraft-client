package com.example.tianyiclient.utils;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class PacketRotation {
    private static long lastPacketTime = 0;
    private static boolean debugMode = false;
    private static long lastLogTime = 0;
    private static final long LOG_INTERVAL = 1000;

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }

    public static boolean isRotationPacket(Packet<?> packet) {
        if (packet == null) return false;
        return packet instanceof PlayerMoveC2SPacket;
    }

    public static Rotation getRotationFromPacket(Packet<?> packet) {
        if (!isRotationPacket(packet)) return null;

        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
            if (movePacket.changesLook()) {
                return new Rotation(movePacket.getYaw(0), movePacket.getPitch(0));
            }
        }
        return null;
    }

    public static Packet<?> modifyPacketRotation(Packet<?> packet, Rotation rotation) {
        if (!isRotationPacket(packet) || rotation == null) {
            return packet;
        }

        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket original = (PlayerMoveC2SPacket) packet;

            if (!original.changesLook()) {
                return packet;
            }

            long currentTime = System.currentTimeMillis();
            boolean shouldLog = debugMode && (currentTime - lastLogTime) > LOG_INTERVAL;

            if (shouldLog) {
                System.out.println("[PacketRotation] 修改前 -> Yaw: " + original.getYaw(0) +
                        ", Pitch: " + original.getPitch(0));
                System.out.println("[PacketRotation] 修改为 -> Yaw: " + rotation.getYaw() +
                        ", Pitch: " + rotation.getPitch());
            }

            PlayerMoveC2SPacket modified = modifyPlayerMovePacket(original, rotation);

            if (modified != original) {
                float newYaw = ((PlayerMoveC2SPacket) modified).getYaw(0);
                float newPitch = ((PlayerMoveC2SPacket) modified).getPitch(0);

                if (shouldLog) {
                    System.out.println("[PacketRotation] ✓ 修改生效 -> Yaw: " + newYaw +
                            ", Pitch: " + newPitch);
                    System.out.println("[PacketRotation] ✓ 包修改成功!");
                    lastLogTime = currentTime;
                }
            } else if (debugMode && shouldLog) {
                System.out.println("[PacketRotation] ⚠ 旋转包未修改");
            }

            return modified;
        }
        return packet;
    }

    private static PlayerMoveC2SPacket modifyPlayerMovePacket(
            PlayerMoveC2SPacket original, Rotation rotation) {

        boolean onGround = original.isOnGround();
        boolean horizontalCollision = original.horizontalCollision();

        if (original.changesPosition() && original.changesLook()) {
            return createFullPacket(
                    original.getX(0), original.getY(0), original.getZ(0),
                    rotation.getYaw(), rotation.getPitch(),
                    onGround,
                    horizontalCollision
            );
        } else if (original.changesLook()) {
            return createLookAndOnGroundPacket(
                    rotation.getYaw(), rotation.getPitch(),
                    onGround,
                    horizontalCollision
            );
        } else if (original.changesPosition()) {
            return createPositionAndOnGroundPacket(
                    original.getX(0), original.getY(0), original.getZ(0),
                    onGround,
                    horizontalCollision
            );
        } else {
            return new PlayerMoveC2SPacket.OnGroundOnly(
                    onGround,
                    horizontalCollision
            );
        }
    }

    public static PlayerMoveC2SPacket.Full createFullPacket(
            double x, double y, double z,
            float yaw, float pitch,
            boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket.Full createFullPacket(
            Vec3d pos, float yaw, float pitch,
            boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.Full(pos, yaw, pitch, onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket.LookAndOnGround createLookAndOnGroundPacket(
            float yaw, float pitch,
            boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket.PositionAndOnGround createPositionAndOnGroundPacket(
            double x, double y, double z,
            boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket.PositionAndOnGround createPositionAndOnGroundPacket(
            Vec3d pos, boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.PositionAndOnGround(pos, onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket.OnGroundOnly createOnGroundOnlyPacket(
            boolean onGround, boolean horizontalCollision) {
        return new PlayerMoveC2SPacket.OnGroundOnly(onGround, horizontalCollision);
    }

    public static PlayerMoveC2SPacket createLookPacket(Vec3d targetPos, boolean onGround) {
        if (net.minecraft.client.MinecraftClient.getInstance().player == null) {
            return null;
        }

        Rotation rotation = Rotation.lookAt(
                net.minecraft.client.MinecraftClient.getInstance().player.getEyePos(),
                targetPos
        );

        return createLookAndOnGroundPacket(rotation.getYaw(), rotation.getPitch(), onGround, false);
    }

    public static PlayerMoveC2SPacket createLookPacket(net.minecraft.entity.Entity entity, boolean onGround) {
        if (entity == null) return null;
        return createLookPacket(entity.getBoundingBox().getCenter(), onGround);
    }

    public static boolean shouldSendRotationPacket() {
        long currentTime = System.currentTimeMillis();
        boolean shouldSend = (currentTime - lastPacketTime) >= 50;
        if (shouldSend) {
            lastPacketTime = currentTime;
        }
        return shouldSend;
    }

    public static Packet<?> modifyPacketRotationReflection(Packet<?> packet, Rotation rotation) {
        if (!isRotationPacket(packet) || rotation == null) {
            return packet;
        }

        if (packet instanceof PlayerMoveC2SPacket) {
            try {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;

                String[] possibleYawFields = {"yaw", "p_", "f_", "field_", "rotationYaw"};
                String[] possiblePitchFields = {"pitch", "p_", "f_", "field_", "rotationPitch"};

                boolean modified = false;

                for (String yawFieldName : possibleYawFields) {
                    for (String pitchFieldName : possiblePitchFields) {
                        try {
                            java.lang.reflect.Field yawField = PlayerMoveC2SPacket.class.getDeclaredField(yawFieldName);
                            java.lang.reflect.Field pitchField = PlayerMoveC2SPacket.class.getDeclaredField(pitchFieldName);

                            yawField.setAccessible(true);
                            pitchField.setAccessible(true);

                            yawField.set(movePacket, rotation.getYaw());
                            pitchField.set(movePacket, rotation.getPitch());

                            modified = true;
                            return movePacket;
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
                    }
                }

            } catch (Exception e) {
                // 静默失败
            }
        }
        return packet;
    }

    public static void debugPacket(Packet<?> packet) {
        if (!(packet instanceof PlayerMoveC2SPacket)) {
            System.out.println("[PacketDebug] 不是PlayerMoveC2SPacket: " + packet.getClass().getSimpleName());
            return;
        }

        PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
        System.out.println("[PacketDebug] === 包信息 ===");
        System.out.println("  Type: " + packet.getClass().getSimpleName());
        System.out.println("  Changes Look: " + movePacket.changesLook());
        System.out.println("  Changes Position: " + movePacket.changesPosition());
        System.out.println("  Yaw: " + movePacket.getYaw(0));
        System.out.println("  Pitch: " + movePacket.getPitch(0));
        System.out.println("  OnGround: " + movePacket.isOnGround());
        System.out.println("  HorizontalCollision: " + movePacket.horizontalCollision());

        if (movePacket.changesPosition()) {
            System.out.println("  X: " + movePacket.getX(0));
            System.out.println("  Y: " + movePacket.getY(0));
            System.out.println("  Z: " + movePacket.getZ(0));
        }
    }
}