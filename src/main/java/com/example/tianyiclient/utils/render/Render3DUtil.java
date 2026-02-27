package com.example.tianyiclient.utils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Render3DUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawOutlinedBox(MatrixStack matrices, Box box, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder buffer = tessellator.begin(
                VertexFormat.DrawMode.LINES,
                VertexFormats.POSITION_COLOR
        );

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // 绘制12条边
        addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, color);
        addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, color);
        addLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, color);
        addLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, color);

        addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, color);
        addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, color);
        addLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        addLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, color);

        addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, color);
        addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, color);
        addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, color);
        addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, color);

        // 直接结束，不调用不存在的类
        BuiltBuffer built = buffer.end();

        // 试试 BuiltBuffer 自己有没有绘制方法
        // built.draw();  // 如果有这个方法就好了

        // 或者看看 Tessellator 有没有 draw 方法
        // tessellator.draw(built);
    }

    private static void addLine(BufferBuilder buffer, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2, int color) {
        buffer.vertex(matrix, x1, y1, z1).color(color);
        buffer.vertex(matrix, x2, y2, z2).color(color);
    }
}