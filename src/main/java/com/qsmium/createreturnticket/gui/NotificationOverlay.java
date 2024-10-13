package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NotificationOverlay
{
    //Class that Serves to display notifications that are sent to the player
    // => THe notification texture that slides in from the right
    //Notifcations can have different "flavors" to them
    // => Different Icons are displayed upon certain notfications
    // => Additionally some notfications might display a kind of "helper" text that the player can expand
    //    when pressing a certain button => I.e [Alt]
    // => As long as the player presses the button the notfication will show more information and not fade away
    //
    // => To implement all of this a 'lite state machine' is used
    //
    //Additionally notifications can be "stacked" up and will be processed one after another


    private enum NotifcationState
    {
        HIDDEN,
        SLIDING_IN,
        SLIDING_OUT,
        STAYING_DEFAULT,
        EXPANDING_INFO,
        CONTRACTING_INFO,
        STAYING_INFO
    }

    public enum Notfications
    {
        GENERIC
    }

    public static List<Notfications> stackedNotifications = new ArrayList<Notfications>();

    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("notification_overlay", new NotificationOverlay.NotificationOverlayScreen());
    }

    private static class NotificationOverlayScreen implements IGuiOverlay
    {

        public final int notificationUVx = 166;
        public final int notificationUVy = 100;
        public final int notificationWidthX = 134;
        public final int notificationHeighY = 33;
        public final int notificationDisplayHeight = 100;
        public final float notificationStayLength = 120;

        public static NotifcationState currentAnimState = NotifcationState.HIDDEN;
        public static float currentAnimTime = 0;



        //Function that renders the actual notification graphics
        //Inside the Render Method we run our "state machine"
        //In this case defined by switch cases lol
        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
        {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            currentAnimTime += partialTick;

            switch (currentAnimState)
            {
                //The Hidden state is when no notifications are shown
                //The state changes to here are
                // SLIDING_OUT -> HIDDEN
                //The state changes from here are
                // HIDDEN -> SLIDING_IN triggered when a notification is added
                //
                //No rendering needs to be done here
                //What needs to be done is:
                // - Check if a new notifcation is added and trigger the state change
                case HIDDEN:

                    if(!stackedNotifications.isEmpty())
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.SLIDING_IN;
                    }

                    break;

                //Sliding in state is when a notification is added and the notification is sliding in
                //The state changes to here are
                // HIDDEN -> SLIDING_IN
                //The state changes from here are
                // SLIDING_IN -> STAYING_DEFAULT triggered automatically when the animation is done
                //
                //What needs to be done:
                // - Render the Notification coming in from the right side
                // - Check how far the animation is => If completed switch to staying_default mode
                case SLIDING_IN:

                    float animLength = 50;


                    float animDuration = currentAnimTime / animLength;
                    int notificationX = Util.easeInOut(screenWidth, screenWidth - notificationWidthX, animDuration);

                    //Display notification
                    guiGraphics.blit(ReturnTicketWidget.TEXTURE, notificationX, notificationDisplayHeight, notificationUVx, notificationUVy, notificationWidthX, notificationHeighY, 512, 256);

                    //Switch states
                    if(animDuration >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.STAYING_DEFAULT;
                    }


                    break;

                //The sliding out state is when the notifcation is done displaying and slides out
                //The state changes to here are
                // STAYING_DEFAULT -> SLIDING_OUT
                //The state changes from here are
                // SLIDING_OUT -> HIDDEN
                //
                //
                case SLIDING_OUT:

                    int notificationX2 = (int) (screenWidth - (notificationWidthX - currentAnimTime));

                    //Display notification
                    guiGraphics.blit(ReturnTicketWidget.TEXTURE, notificationX2, notificationDisplayHeight, notificationUVx, notificationUVy, notificationWidthX, notificationHeighY, 512, 256);

                    //Switch states
                    if(notificationX2 >= screenWidth)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.HIDDEN;
                    }

                    break;

                //The staying default state is when the notification is shown and stays here for a while
                //When the player wants more information we expand the info from here
                //The state changes to here are
                // SLIDING_IN -> STAYING_DEFAULT
                // CONTRACTING_INFO -> STAYING_DEFAULT
                //The state changes from here are
                // STAYING_DEFAULT -> SLIDING_OUT
                // STAYING_DEFAULT -> EXPANDING_INFO
                //
                //
                case STAYING_DEFAULT:

                    //Display notification
                    guiGraphics.blit(ReturnTicketWidget.TEXTURE, screenWidth - notificationWidthX, notificationDisplayHeight, notificationUVx, notificationUVy, notificationWidthX, notificationHeighY, 512, 256);

                    //Switch states
                    if(currentAnimTime >= notificationStayLength)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.SLIDING_OUT;
                    }

                    break;

                //The expanding info state is when the player wants more information from the notifcation
                //And they press the button to show it => I.e this is the animation that plays
                //The state changes to here are
                // STAYING_DEFAULT -> EXPANDING_INFO
                //The state changes from here are
                // EXPANDING_INFO -> CONTRACTING_INFO
                //
                //
                case EXPANDING_INFO:

                    break;

                //The contracting info state is when the player is done looking at info
                //The extra information tab moves out of the way
                //The state changes to her are
                // STAYING_INFO -> CONTRACTING_INFO
                //The state changes from here are
                // CONTRACING_INFO -> STAYING_DEFAULT
                //
                //
                case CONTRACTING_INFO:

                    break;

                //The staying info state stays for as long as the player wants to look at the extra information
                //The state changes to here are
                // EXPANDING_INFO -> STAYING_INFO
                //The state changes from here are
                // STAYING_INFO -> CONTRACTING_INFO
                case STAYING_INFO:

                    break;
            }

            poseStack.popPose();

        }
    }

    public static void addNotification(NotificationOverlay.Notfications newNotification)
    {
        stackedNotifications.add(newNotification);
    }



}
