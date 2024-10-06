package com.qsmium.createreturnticket.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Mod.EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class RenderEventHandler {
    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            PoseStack poseStack = event.getPoseStack();
            BlockPos pos = new BlockPos(0, 64, 0); // Example position
            BlockHighlightRenderer.renderHighlight(poseStack, pos, event.getPartialTick());
        }
    }
}