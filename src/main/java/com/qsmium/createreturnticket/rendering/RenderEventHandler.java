package com.qsmium.createreturnticket.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.Util;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class RenderEventHandler {

    private static final long BLOCK_SHOW_TIME = 10000; //In Millis
    private static final long FADE_IN_TIME = 500;
    private static final long FADE_OUT_TIME = 1000;

    private static BlockPos m_highLightPos = new BlockPos(0, 0, 0);

    private static long m_timeLast = 0;
    private static long m_timeLeftForBLockShow = 0;


    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS && m_timeLeftForBLockShow > 0) {
            PoseStack poseStack = event.getPoseStack();

            //Calculate the alpha for fade out/in
            float fadeOutAlpha = m_timeLeftForBLockShow < FADE_OUT_TIME ? Util.easeInOutFloat(1, 0, 1.0f - ((float) m_timeLeftForBLockShow / (float) FADE_OUT_TIME)) : 1.1f;
            long fadeInStopTime = BLOCK_SHOW_TIME - FADE_IN_TIME;
            float fadeInAlpha = fadeInStopTime < m_timeLeftForBLockShow ? Util.easeInOutFloat(0, 1, 1.0f - (((float) m_timeLeftForBLockShow - (float)fadeInStopTime) / (float) FADE_IN_TIME)) : 1.1f;
            float alpha = fadeInAlpha < 1 ? fadeInAlpha : fadeOutAlpha < 1 ? fadeOutAlpha : 1;

            //Render the Highlight
            BlockHighlightRenderer.renderHighlight(poseStack, m_highLightPos, event.getPartialTick().getRealtimeDeltaTicks(), alpha);
        }
    }

    //Function that counts down the timer for the block highlighting to disappear
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (m_timeLeftForBLockShow > 0) {

            long currentTime = System.currentTimeMillis();
            long timePassedSinceLast = m_timeLast > 0 ? currentTime - m_timeLast : 0;

            m_timeLast = currentTime;
            m_timeLeftForBLockShow -= timePassedSinceLast;
        }
    }

    public static void showBlock(BlockPos blockPos)
    {
        m_highLightPos = blockPos;
        m_timeLeftForBLockShow = BLOCK_SHOW_TIME;

        //TODO: Change this shit to delta time or sth
        m_timeLast = 0;

    }
}