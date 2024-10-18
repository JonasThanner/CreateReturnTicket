package com.qsmium.createreturnticket.rendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;

public class BlockHighlightRenderer {
    private static float hue = 0.0f;

    public static void renderHighlight(PoseStack poseStack, BlockPos pos, float partialTick, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        BlockAndTintGetter world = mc.level;
        BlockState state = world.getBlockState(pos);

        // Check if the block is air or has no defined shape
        VoxelShape shape = state.getShape(world, pos);
        AABB aabb = shape.isEmpty() ? new AABB(pos).inflate(0.01) : shape.bounds().move(pos); // Inflate slightly to avoid a 0-sized box

        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        hue += 0.005; // Adjust the speed of the rainbow effect
        if (hue > 1.0) hue -= 1.0;
        int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        float r = (rgb >> 16 & 0xFF) / 255.0f;
        float g = (rgb >> 8 & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(HighlightRenderType.OUTLINE);

        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aabb, r, g, b, alpha);
        poseStack.popPose();
    }
}