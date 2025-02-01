package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TransitOverlay
{
    public static final ResourceLocation MASK_TEX = new ResourceLocation(ModMain.MODID,"textures/mask.png");
    public static final ResourceLocation BACKGROUND_TEX = new ResourceLocation(ModMain.MODID,"textures/background.png");

    public static final ResourceLocation AXOLOTL_TEX = new ResourceLocation(ModMain.MODID,"textures/axolotl_transit_animation.png");
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
        private static final int BACKGROUND_UV_SIZE = 256;
        private static final int AXOLOTL_UV_WIDTH = 162;
        private  static final int AXOLOTL_UV_HEIGHT = 72;
        private static final int CLOSER_UV_SIZE = 50;
        private static final int AXOTRACKS_UV_WIDTH = 126;
        private static final int AXOTRACKS_UV_HEIGHT = 92;
        private static final int AXOTRACKS_UV_X = 0;
        private static final int AXOTRACKS_UV_Y = 119;


        private static int animationState = 1;
        private static float globalAnimTime = 0; //For Tracking how long during the entire animation stage
        private static float currentAnimTime = 0; //Tracking how long inside each animation function
        private static int arrowAnimCycle = 0; //Tracking what cycle of animation the arrow anim is currently in
        private static int axolotlAnimCycle = 0; //Needed for the Axolotl Animation
        private static float axolotlAnimTime = 0;
        private static boolean axolotlAnimPingPongDecrease = false;
        private static boolean animationRunning = false;

        //Arrow Stuff
        private static int arrowY = 100;
        private static int arrowMiddle = 100;


        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
        {
            //Animation Setup Stuff
            if(startAnimation)
            {
                startAnimation = false;
                animationRunning = true;

                arrowY = screenHeight / 8;
                arrowMiddle = arrowY + (ARROW_UV_HEIGHT / 2);
                arrowAnimCycle = 0;
                currentAnimTime = 0;
                animationState = 1;
                globalAnimTime = 0;

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
                    //WHen this is done that means the entire screen is covered now
                    // => Time to send the redeem request
                    Util.setupStencilMask();
                    RenderSystem.setShaderTexture(0, MASK_TEX);
                    if(animateFullscreenCovers(guiGraphics, partialTick, 0.2f, screenWidth,screenHeight))
                    {
                        globalAnimTime = 0;
                        animationState = 3;

                        //Send Redeem Request
                        ReturnTicketPacketHandler.sendRedeem();
                    }

                    Util.setupStencilTexture();
                    RenderSystem.setShaderTexture(0, BACKGROUND_TEX);
                    animateBackground(guiGraphics, partialTick, 0.3f, screenWidth, screenHeight);

                    RenderSystem.setShaderTexture(0, AXOLOTL_TEX);
                    //Animate Axolotl
                    animateAxolotl(guiGraphics, partialTick, 8f, screenWidth , screenHeight);

                    Util.disableStencil();



                    //Draw Animating Arrows
                    animateArrowToDirection(guiGraphics, partialTick, screenWidth, screenHeight);


                    break;

                case 3:

                    globalAnimTime += partialTick;

                    if(globalAnimTime > 200)
                    {
                        animationState = 4;
                        globalAnimTime = 0;
                    }


                    //animate Background
                    animateBackground(guiGraphics, partialTick, 0.3f, screenWidth, screenHeight);

                    //Animate Axolotl
                    animateAxolotl(guiGraphics, partialTick, 8f, screenWidth , screenHeight);

                    //Draw Animating Arrows
                    animateArrowToDirection(guiGraphics, partialTick, screenWidth, screenHeight);



                    break;

                case 4:

                    globalAnimTime += partialTick;


                    //Animate closing
                    Util.setupStencilMask();
                    RenderSystem.setShaderTexture(0, ReturnTicketWidget.TEXTURE);
                    if(animateCloser(guiGraphics, partialTick, 1f,screenWidth, screenHeight))
                    {
                        animationRunning = false;
                        Util.setupStencilTexture(GL11.GL_NOTEQUAL);
                        Util.disableStencil();
                        break;
                    }

                    Util.setupStencilTexture(GL11.GL_NOTEQUAL);
                    RenderSystem.setShaderTexture(0, BACKGROUND_TEX);
                    animateBackground(guiGraphics, partialTick, 0.3f, screenWidth, screenHeight);

                    //Animate Axolotl
                    RenderSystem.setShaderTexture(0, AXOLOTL_TEX);
                    animateAxolotl(guiGraphics, partialTick, 8f, screenWidth , screenHeight);

                    //Draw Animating Arrows
                    RenderSystem.setShaderTexture(0, ReturnTicketWidget.TEXTURE);
                    animateArrowToDirection(guiGraphics, partialTick, screenWidth, screenHeight);

                    Util.disableStencil();


                    break;
            }
            guiGraphics.pose().popPose();


        }

        public static boolean animateFullscreenCovers(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            PoseStack poseStack = guiGraphics.pose();

            float coversYScale = ((globalAnimTime * animSpeed) % 50) * 0.1f;


            //================ Upper Covers ===========================
            poseStack.pushPose();
            Util.SafeScale(poseStack, 100, coversYScale, -10, arrowMiddle);
            guiGraphics.blit(MASK_TEX, -10, arrowMiddle, 0, 0, 256, 256, 256, 256);
            poseStack.popPose();

            poseStack.pushPose();
            Util.SafeScale(poseStack, 100, -coversYScale, -10, arrowMiddle, 256, 256);
            guiGraphics.blit(MASK_TEX, -10, arrowMiddle, 0, 0, 256, 256, 256, 256);
            poseStack.popPose();
            //================ Upper Covers ===========================

            if(coversYScale >= 48.0f * 0.1f)
            {
                return  true;
            }

            return false;

        }

        //Animates the circle that gets bigger and bigger to end the transit animation
        public static boolean animateCloser(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            //The time in ticks the anim should take up
            float animTimeToTake = 150 / animSpeed;

            //Calculate the progress of the closer in a 0-1 range
            float animProgress = globalAnimTime / animTimeToTake;

            //Interpolate scales values
            Pair<Double, Double> point1 = new Pair<Double, Double>(0.0, 0.0);
            Pair<Double, Double> point2 = new Pair<Double, Double>(0.2, 1.0);
            Pair<Double, Double> point3 = new Pair<Double, Double>(0.3, 0.5);
            Pair<Double, Double> point4 = new Pair<Double, Double>(1.0, 20.0);
            List<Pair<Double, Double>> pointList = new ArrayList<>();
            pointList.add(point1);
            pointList.add(point2);
            pointList.add(point3);
            pointList.add(point4);
            float scale = Util.cubicInterpolation(pointList, animProgress);

            //Pose stack stuff
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            //Scale the closer circle
            Util.SafeScaleFromMiddle(poseStack, scale, scale, (screenWidth / 2) - (CLOSER_UV_SIZE / 2), (screenHeight / 2) - (CLOSER_UV_SIZE / 2), CLOSER_UV_SIZE, CLOSER_UV_SIZE);

            //Draw Closer Circle
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, (screenWidth / 2) - (CLOSER_UV_SIZE / 2), (screenHeight / 2) - (CLOSER_UV_SIZE / 2), 301, 100, CLOSER_UV_SIZE, CLOSER_UV_SIZE, 512, 256);
            poseStack.popPose();

            //Check if animation is run
            if(globalAnimTime >= animTimeToTake)
            {
                return true;
            }



            return false;
        }

        public static void animateAxolotl(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            //The Timings of each of the Axolotl Animation frames
            // => In total 9 Frames
            int[] frameTimes = {100, 100, 50, 40, 30, 40, 50, 100, 100};

            axolotlAnimTime += partialTick;

            //Going to next Frame Logic
            if(axolotlAnimTime > frameTimes[axolotlAnimCycle] / animSpeed)
            {
                axolotlAnimTime = 0;

                //Ping pong
                // => If decreasing animState we check if when subtracting we are below 0
                // => If increasing animState we check if when adding we are above 8

                axolotlAnimCycle = axolotlAnimPingPongDecrease ? axolotlAnimCycle - 1 : axolotlAnimCycle + 1;

                //Revert behavior if at ping pong point
                if(axolotlAnimCycle > 8 || axolotlAnimCycle < 0)
                {
                    axolotlAnimCycle = axolotlAnimPingPongDecrease ? axolotlAnimCycle + 2: axolotlAnimCycle - 2;
                    axolotlAnimPingPongDecrease = !axolotlAnimPingPongDecrease;
                }
            }

            //Calculate Axolotl Draw Position
            // => Should be so that Axoltl is at center of screen
            int axoX = (screenWidth / 2) - (AXOLOTL_UV_WIDTH / 2);
            int axoY = (screenHeight / 2) - (AXOLOTL_UV_HEIGHT / 2) - 25;

            //Calculate AxolotlUV Coordinates
            int axoUVX = AXOLOTL_UV_WIDTH * ((axolotlAnimCycle) % 3);
            int axoUVY = AXOLOTL_UV_HEIGHT * (int) (Math.floor((double) axolotlAnimCycle) / 3.0);

            //Scale down AxoTracks logo
            guiGraphics.pose().pushPose();

            //Drawing Axolotl
            guiGraphics.blit(AXOLOTL_TEX, axoX, axoY, axoUVX, axoUVY, AXOLOTL_UV_WIDTH, AXOLOTL_UV_HEIGHT, 512, 256);

            //Draw AxoTracks Logo
            guiGraphics.blit(ReturnTicketWindow.TEXTURE2, axoX + +22, axoY + AXOLOTL_UV_HEIGHT, AXOTRACKS_UV_X, AXOTRACKS_UV_Y, AXOTRACKS_UV_WIDTH, AXOTRACKS_UV_HEIGHT, 512, 256);

            Util.SafeScaleFromMiddle(guiGraphics.pose(), 0.5f, 0.5f, axoX, axoY, 512, 256);

            guiGraphics.pose().popPose();
        }

        public static boolean animateBackground(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            //Frame count for background is 15
            int animPos = (int) Math.floor((double) (globalAnimTime * animSpeed) % 15);


            PoseStack poseStack = guiGraphics.pose();



            Util.drawRepatingBlit(guiGraphics, BACKGROUND_TEX, -16 + animPos, -16 + animPos, 0, 0, 256, 256, 256, 256, 4, 4);


            return false;
        }

        public static boolean arrowSlideInAnim(GuiGraphics guiGraphics, float partialTick, float animSpeed, int screenWidth, int screenHeight)
        {
            //Increase Animation Time
            currentAnimTime += partialTick;

            //Adjust Anim Speed for screenSize
            //TODO


            //Draw Top Arrow Tip
            guiGraphics.blit(ReturnTicketWindow.TEXTURE, 0 + (int)(currentAnimTime * animSpeed), arrowY, 228, 133, 12, ARROW_UV_HEIGHT, 512, 256);

            //Draw Bottom Arrow Tip
            int bottomArrowY = screenHeight - arrowY - ARROW_UV_HEIGHT;
            guiGraphics.blit(ReturnTicketWindow.TEXTURE, screenWidth - (int)(currentAnimTime * animSpeed), bottomArrowY, 284, 150, 12, ARROW_UV_HEIGHT, 512, 256);

            //Calculate how many arrow tex needed for anim
            int arrowCount = (screenWidth / ARROW_UV_WIDTH) + 1;

            //Draw Arrow Train
            //Has to be trailing behind Arrow Top
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, 0 + (int)(currentAnimTime * animSpeed) - (arrowCount * 50), arrowY, 178, 133, ARROW_UV_WIDTH, ARROW_UV_HEIGHT, 512, 256, arrowCount, 1);

            //Draw bottom ArrowTrain
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, screenWidth - (int)(currentAnimTime * animSpeed) + 12, bottomArrowY, 296, 150, ARROW_UV_WIDTH, ARROW_UV_HEIGHT, 512, 256, arrowCount, 1);

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

        public static void animateArrowToDirection(GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
        {
            //Curret anim cycle logic
            currentAnimTime += partialTick;
            if(currentAnimTime > 5)
            {
                currentAnimTime = 0;
                arrowAnimCycle++;
                if(arrowAnimCycle > 9)
                {
                    arrowAnimCycle = 0;
                }
            }

            //Draw anim Cycle
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, 0 + arrowAnimCycle - ARROW_UV_WIDTH, arrowY, 178, 133, ARROW_UV_WIDTH, ARROW_UV_HEIGHT, 512, 256, (screenWidth / ARROW_UV_WIDTH) + 2, 1);

            //Draw bottom ArrowTrain
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWindow.TEXTURE, (-arrowAnimCycle) - ARROW_UV_WIDTH, screenHeight - arrowY - ARROW_UV_HEIGHT, 296, 150, ARROW_UV_WIDTH, ARROW_UV_HEIGHT, 512, 256, (screenWidth / ARROW_UV_WIDTH) + 2, 1);


        }

    }






}
