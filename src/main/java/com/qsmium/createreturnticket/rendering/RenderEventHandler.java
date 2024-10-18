package com.qsmium.createreturnticket.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.TicketManager;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.gui.NotificationOverlay;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class RenderEventHandler {

    private static final long blockShowTime = 10000; //In Millis
    private static final long fadeInTime = 500;
    private static final long fadeOutTime = 1000;

    private static BlockPos highLightPos = new BlockPos(0, 0, 0);

    private static long timeLast = 0;
    private static long timeLeftForBLockShow = 0;

    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS && timeLeftForBLockShow > 0) {
            PoseStack poseStack = event.getPoseStack();

            //Calculate the alpha for fade out/in
            float fadeOutAlpha = timeLeftForBLockShow < fadeOutTime ? Util.easeInOutFloat(1, 0, 1.0f - ((float)timeLeftForBLockShow / (float)fadeOutTime)) : 1.1f;
            long fadeInStopTime = blockShowTime - fadeInTime;
            float fadeInAlpha = fadeInStopTime < timeLeftForBLockShow ? Util.easeInOutFloat(0, 1, 1.0f - (((float)timeLeftForBLockShow - (float)fadeInStopTime) / (float)fadeInTime)) : 1.1f;
            float alpha = fadeInAlpha < 1 ? fadeInAlpha : fadeOutAlpha < 1 ? fadeOutAlpha : 1;

            //Render the Highlight
            BlockHighlightRenderer.renderHighlight(poseStack, highLightPos, event.getPartialTick(), alpha);
        }
    }

    //Function that counts down the timer for the block highlighting to disappear
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && timeLeftForBLockShow > 0) {

            long currentTime = System.currentTimeMillis();
            long timePassedSinceLast = timeLast > 0 ? currentTime - timeLast : 0;

            timeLast = currentTime;
            timeLeftForBLockShow -= timePassedSinceLast;
        }
    }

    @SubscribeEvent
    public static void onUseEvent(PlayerInteractEvent.RightClickItem event)
    {
        if(event.getItemStack().is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "cobblestone"))))
        {
            showBlock(new BlockPos(0, 100, 0));
        }

    }

    public static void showBlock(BlockPos blockPos)
    {
        highLightPos = blockPos;
        timeLeftForBLockShow = blockShowTime;

        //TODO: Change this shit to delta time or sth
        timeLast = 0;

    }
}