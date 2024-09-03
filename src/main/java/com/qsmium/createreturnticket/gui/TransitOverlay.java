package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.TicketManager;
import com.qsmium.createreturnticket.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.swing.*;

@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TransitOverlay
{
    public static final ResourceLocation MASK_TEX = new ResourceLocation(ModMain.MODID,"textures/mask.png");

    public static boolean startAnimation = false;
    public static Action teleportStart;



    //Arrow Section


    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("transit_overlay", new TransitScreenOverlay());
    }

    private static class TransitScreenOverlay implements IGuiOverlay
    {
        private static final int ARROW_UV_WIDTH = 50;
        private static final int ARROW_UV_HEIGHT = 18;
        private static final int ARROW_TIP_UV_WIDTH = 12;


        private static int animationState = 1;
        private static float globalAnimTime = 0; //For Tracking how long during the entire animation stage
        private static float currentAnimTime = 0; //Tracking how long inside each animation function
        private static int currentAnimCycle = 0; //Tracking what cycle of animation the arrow anim is currently in
        private static boolean animationRunning = false;

        //Arrow Stuff
        private static int arrowY = 100;


        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
        {
            //Animation Setup Stuff
            if(startAnimation)
            {
                startAnimation = false;
                animationRunning = true;

                arrowY = screenHeight / 8;
                currentAnimCycle = 0;
                currentAnimTime = 0;
                animationState = 1;
            }

            if(!animationRunning)
            {
                return;
            }

            //guiGraphics.blit(ReturnTicketWindow.TEXTURE, 0, 0, 0, 100, 150, 150, 512, 256);
            //animateArrowToDirection(guiGraphics, partialTick);


            //Do different things depending on animation state
            //Animation State 1
            // => Move Arrows
            //Animation State 2
            // => Animate Arrows
            // => Animate Background
            // => Animate Axolotls
            // => Move in FullScreenCovers
            //Animation State 3 => State Transition => Send Feedback for teleport
            // => Animate Arrows
            // => Animate Background
            // => Animate Axolotl
            guiGraphics.pose().pushPose();
            switch (animationState)
            {
                case 1:
                    if(arrowSlideInAnim(guiGraphics, partialTick, 10, screenWidth, screenHeight))
                    {
                        animationState = 2;
                    }
                    break;

                case 2:

                    globalAnimTime += partialTick;



                    //Move in FullScreenCovers
                    //Because they are stencils for everything coming after they need to be drawn now
                    animateFullscreenCovers(guiGraphics, partialTick, 1, screenWidth,screenHeight);



                    //Draw Animating Arrows
                    animateArrowToDirection(guiGraphics, partialTick, screenWidth);


                    break;

                case 3:

                    break;
            }
            guiGraphics.pose().popPose();


        }

        public static boolean animateFullscreenCovers(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            PoseStack poseStack = guiGraphics.pose();

            poseStack.pushPose();

            guiGraphics.blit(MASK_TEX, 50, 50, 0, 0, 256, 256, 256, 256);

            poseStack.pushTransformation(new Transformation(new Vector3f(0,0,0), new Quaternionf(0, 0, 0, 0), new Vector3f(2, 0.5f, 3), new Quaternionf(0,0,0,0)));

            poseStack.popPose();

            return false;

        }

        public static boolean arrowSlideInAnim(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            //Increase Animation Time
            currentAnimTime += partialTick;

            //Adjust Anim Speed for screenSize
            //TODO




            //Draw Arrow Tip
            guiGraphics.blit(ReturnTicketWindow.TEXTURE, 0 + (int)(currentAnimTime * animSpeed), arrowY, 228, 133, 12, 18, 512, 256);

            //Calculate how many arrow tex needed for anim
            int arrowCount = (screenWidth / ARROW_UV_WIDTH) + 1;

            //Draw Arrow Train
            //Has to be trailing behind Arrow Top
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, 0 + (int)(currentAnimTime * animSpeed) - (arrowCount * 50), arrowY, 178, 133, 50, 18, 512, 256, arrowCount, 1);

            //Calculate Max Animation
            // => Max Animation is when the Arrowtrain is covering the whole screen
            // i.e when the animationTime has move the whole thing to the side enough so that its bigger than the screen
            if((currentAnimTime * animSpeed) > (screenWidth + ARROW_TIP_UV_WIDTH))
            {
                currentAnimTime = 0;
                return true;
            }

            return false;

        }

        public static void animateArrowToDirection(GuiGraphics guiGraphics, float partialTick, int screenWidth)
        {
            //Curret anim cycle logic
            currentAnimTime += partialTick;
            if(currentAnimTime > 5)
            {
                currentAnimTime = 0;
                currentAnimCycle++;
                if(currentAnimCycle > 9)
                {
                    currentAnimCycle = 0;
                }
            }

            //Draw anim Cycle
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, 0 + currentAnimCycle - ARROW_UV_WIDTH, arrowY, 178, 133, 50, 18, 512, 256, (screenWidth / ARROW_UV_WIDTH) + 2, 1);


        }

    }






}
