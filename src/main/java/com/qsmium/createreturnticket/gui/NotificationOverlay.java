package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.Keybinds;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.NotificationManager;
import com.qsmium.createreturnticket.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

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
        EXPANDING_INFO_MINI,
        CONTRACTING_INFO_MINI,
        STAYING_DEFAULT,
        EXPANDING_INFO,
        CONTRACTING_INFO,
        STAYING_INFO
    }

    public static boolean expandInfoKeyClicked = false;
    public static List<NotificationManager.CRTNotification> stackedNotifications = new ArrayList<NotificationManager.CRTNotification>();

    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("notification_overlay", new NotificationOverlay.NotificationOverlayScreen());
    }

    private static class NotificationOverlayScreen implements IGuiOverlay
    {

        private final int notificationUVx = 166;
        private final int notificationUVy = 100;
        private final int notificationWidthX = 134;
        private final int notificationHeighY = 31;
        private final int notificationDisplayHeight = 100;
        private final static int NOTIFICATION_STAY_LENGTH_MASTER = 120;
        private float notificationStayLength = 120;
        private final int notificationTextWidth = 93;

        private final int miniInfoReminderUVx = 470;
        private final int miniInfoReminderUVy = 0;
        private final int miniInfoWidthX = 32;
        private final int miniInfoHeightY = 12;
        private final int miniInfoTextWidth = 26; //Max width that the text inside the box is allowed to take up

        private final int bigInfoTopUVx = 167;
        private final int bigInfoTopUVy = 151;
        private final int bigInfoTopWidthX = 118;
        private final int bigInfoTopHeightY = 2;

        private final int bigInfoMiddleUVx = 167;
        private final int bigInfoMiddleUVy = 153;
        private final int bigInfoMiddleHeightY = 9;

        private final int bigInfoBottomUVx = 167;
        private final int bigInfoBottomUVy = 176;
        private final int bigInfoBottomHeightY = 8;
        private final int bigInfoTextWidth = 110;

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

            //Variable adjustments
            currentAnimTime += partialTick;

            //If we have more than one notification waiting, we severly reduce the notifcation stay time
            notificationStayLength = stackedNotifications.size() > 1 ? NOTIFICATION_STAY_LENGTH_MASTER / 10 : NOTIFICATION_STAY_LENGTH_MASTER;

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
                // SLIDING_IN -> EXPANDING_INFO_MINI triggered automatically when the animation is done
                //
                //What needs to be done:
                // - Render the Notification coming in from the right side
                // - Check how far the animation is => If completed switch to staying_default mode
                case SLIDING_IN:

                    float animLength = 50;


                    float animDuration = currentAnimTime / animLength;
                    int notificationX = Util.easeInOut(screenWidth, screenWidth - notificationWidthX, animDuration);

                    //Display notification
                    drawTopNotification(guiGraphics, notificationX, notificationDisplayHeight);
                    drawNotificationShadow(guiGraphics, notificationX, notificationDisplayHeight);

                    //Switch states
                    if(animDuration >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.EXPANDING_INFO_MINI;
                    }


                    break;

                //The sliding out state is when the notifcation is done displaying and slides out
                //The state changes to here are
                // CONTRACTING_INFO_MINI -> SLIDING_OUT
                //The state changes from here are
                // SLIDING_OUT -> HIDDEN
                //
                //
                case SLIDING_OUT:

                    float animLength2 = 50;
                    float animDuration2 = currentAnimTime / animLength2;
                    int notificationX2 = Util.easeInOut(screenWidth - notificationWidthX, screenWidth, animDuration2);

                    //Display notification
                    drawTopNotification(guiGraphics, notificationX2, notificationDisplayHeight);
                    drawNotificationShadow(guiGraphics, notificationX2, notificationDisplayHeight);

                    //Switch states
                    if(animDuration2 >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.HIDDEN;

                        //Delete topmost notification
                        stackedNotifications.remove(0);
                    }

                    break;


                //Expanding info mini is for popping out the explainer that show the player the key to press to expand for more info
                //The state changes to here are
                // SLIDING_OUT -> EXPANDING_INFO_MINI
                //The state changes from here are
                // EXPANDING_INFO_MINI -> STAYING_DEFAULT
                //
                //What we need to do is
                // - Render the normal notification where it is supposed to be
                // - Expand out the Mini explainer image
                case EXPANDING_INFO_MINI:

                    //Display notification shadow
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Pop out mini info
                    float animLength3 = 20;
                    float animDuration3 = currentAnimTime / animLength3;
                    int miniY = Util.easeInOut(0, miniInfoHeightY, animDuration3);

                    //Draw Mini notifcation
                    drawMiniNotifier(guiGraphics, screenWidth - notificationWidthX + 6, notificationDisplayHeight + notificationHeighY - miniInfoHeightY + miniY);

                    //Draw notification
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Transition
                    if(animDuration3 >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.STAYING_DEFAULT;
                    }


                    break;


                //Contracting info mini is for hiding the explainer that shows the player the key they need to press to expand for more informationn
                //The state changes to here are
                // STAYING_DEFAULT -> CONTRACTING_INFO_MINI
                //The state changes from here are
                // CONTRACTING_INFO_MINI -> SLIDING_OUT
                //
                //
                case CONTRACTING_INFO_MINI:

                    //Display notf Shadow
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Pop out mini info
                    float animLength4 = 15;
                    float animDuration4 = currentAnimTime / animLength4;
                    int miniY2 = Util.easeInOut(miniInfoHeightY, 0, animDuration4);

                    //Draw Mini notifcation
                    drawMiniNotifier(guiGraphics, screenWidth - notificationWidthX + 6, notificationDisplayHeight + notificationHeighY - miniInfoHeightY + miniY2);

                    //Draw normal notification ontop
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Transition
                    if(animDuration4 >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.SLIDING_OUT;
                    }

                    break;



                //The staying default state is when the notification is shown and stays here for a while
                //When the player wants more information we expand the info from here
                //The state changes to here are
                // EXPANDING_INFO_MINI -> STAYING_DEFAULT
                // CONTRACTING_INFO -> STAYING_DEFAULT
                //The state changes from here are
                // STAYING_DEFAULT -> CONTRACTING_INFO_MINI
                // STAYING_DEFAULT -> EXPANDING_INFO
                //
                //
                case STAYING_DEFAULT:

                    //Display notification
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);
                    drawMiniNotifier(guiGraphics, screenWidth - notificationWidthX + 6, notificationDisplayHeight + notificationHeighY);
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Switch states
                    if(expandInfoKeyClicked)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.EXPANDING_INFO;
                        break;
                    }

                    if (currentAnimTime >= notificationStayLength)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.CONTRACTING_INFO_MINI;
                    }

                    break;

                //The expanding info state is when the player wants more information from the notifcation
                //And they press the button to show it => I.e this is the animation that plays
                //The state changes to here are
                // STAYING_DEFAULT -> EXPANDING_INFO
                //The state changes from here are
                // EXPANDING_INFO -> CONTRACTING_INFO
                //
                //To expand the info we need to
                // - Render Notification Shadow
                // - Render Mini Notification -> Make it disappear
                // - Enable Stencil Texture
                // - Render Mask as Stencil so that only the area under the notification is visible
                // - Disable Stencil
                // - Render Normal Notification
                case EXPANDING_INFO:

                    //Render Notification Shadow
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Make Mini Notification Disappear
                    float infoExMiniAnimLength = 15;
                    float infoExMiniAnimDuration = currentAnimTime / infoExMiniAnimLength;
                    infoExMiniAnimDuration = infoExMiniAnimDuration > 1 ? 1 : infoExMiniAnimDuration;
                    int miniInfoExY = Util.easeInOut(notificationDisplayHeight + notificationHeighY, notificationDisplayHeight + notificationHeighY - miniInfoHeightY, infoExMiniAnimDuration);
                    drawMiniNotifier(guiGraphics, screenWidth - notificationWidthX + 6, miniInfoExY);

                    //Enable Stencil and draw it
                    Util.setupStencilMask();
                    RenderSystem.setShaderTexture(0, TransitOverlay.MASK_TEX);
                    guiGraphics.blit(TransitOverlay.MASK_TEX, screenWidth - notificationWidthX, notificationDisplayHeight + notificationHeighY, 0, 0, 256, 256, 256, 256);

                    //Draw Thingy that should be stenciled
                    Util.setupStencilTexture(GL11.GL_EQUAL);
                    RenderSystem.setShaderTexture(0, ReturnTicketWidget.TEXTURE);

                    float infoExAnimLength = 30;
                    float infoExAnimDuration = currentAnimTime / infoExAnimLength;
                    int infoExY = Util.easeInOut(notificationDisplayHeight + notificationHeighY - bigInfoHeight(), notificationDisplayHeight + notificationHeighY, infoExAnimDuration);
                    drawBigInfo(guiGraphics, screenWidth - notificationWidthX + 20, infoExY);
                    Util.disableStencil();

                    //Render Normal Notification
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    if(infoExAnimDuration >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.STAYING_INFO;
                    }


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

                    //Has to be up here for minianim to use it
                    float infoContrAnimLength = 30;

                    //Render Notification Shadow
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Make Mini Notification Disappear
                    float infoContrMiniAnimLength = 15;
                    float infoContrMiniAnimDuration = 0;
                    if(currentAnimTime > infoContrAnimLength - infoContrMiniAnimLength)
                    {
                        infoContrMiniAnimDuration = (currentAnimTime - infoContrMiniAnimLength) / infoContrMiniAnimLength;
                    }
                    infoExMiniAnimDuration = infoContrMiniAnimDuration > 1 ? 1 : infoContrMiniAnimDuration;
                    int miniInfoContrY = Util.easeInOut(notificationDisplayHeight + notificationHeighY - miniInfoHeightY, notificationDisplayHeight + notificationHeighY, infoExMiniAnimDuration);
                    drawMiniNotifier(guiGraphics, screenWidth - notificationWidthX + 6, miniInfoContrY);

                    //Enable Stencil and draw it
                    Util.setupStencilMask();
                    RenderSystem.setShaderTexture(0, TransitOverlay.MASK_TEX);
                    guiGraphics.blit(TransitOverlay.MASK_TEX, screenWidth - notificationWidthX, notificationDisplayHeight + notificationHeighY, 0, 0, 256, 256, 256, 256);

                    //Draw Thingy that should be stenciled
                    Util.setupStencilTexture(GL11.GL_EQUAL);
                    RenderSystem.setShaderTexture(0, ReturnTicketWidget.TEXTURE);


                    float infoContrAnimDuration = currentAnimTime / infoContrAnimLength;
                    int infoContrY = Util.easeInOut(notificationDisplayHeight + notificationHeighY, notificationDisplayHeight + notificationHeighY - bigInfoHeight(), infoContrAnimDuration);
                    drawBigInfo(guiGraphics, screenWidth - notificationWidthX + 20, infoContrY);
                    Util.disableStencil();

                    //Render Normal Notification
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    if(infoContrAnimDuration >= 1)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.STAYING_DEFAULT;
                    }

                    break;

                //The staying info state stays for as long as the player wants to look at the extra information
                //The state changes to here are
                // EXPANDING_INFO -> STAYING_INFO
                //The state changes from here are
                // STAYING_INFO -> CONTRACTING_INFO
                case STAYING_INFO:

                    //Render notification shadow
                    drawNotificationShadow(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    //Render notification info
                    drawBigInfo(guiGraphics, screenWidth - notificationWidthX + 20, notificationDisplayHeight + notificationHeighY);

                    //Render notification
                    drawTopNotification(guiGraphics, screenWidth - notificationWidthX, notificationDisplayHeight);

                    if(expandInfoKeyClicked)
                    {
                        currentAnimTime = 0;
                    }

                    if(currentAnimTime > notificationStayLength)
                    {
                        currentAnimTime = 0;
                        currentAnimState = NotifcationState.CONTRACTING_INFO;
                    }

                    break;
            }

            poseStack.popPose();

        }

        private void drawTopNotification(GuiGraphics guiGraphics, int x, int y)
        {

            //Draw Notification
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, x, y, notificationUVx, notificationUVy, notificationWidthX, notificationHeighY, 512, 256);

            //Create Text
            Font font = Minecraft.getInstance().font;
            String text = Component.translatable(stackedNotifications.get(0).notifcationShort).getString();
            int color = 0x6ac6ae;

            //Turn the text into two lines based on length
            List<FormattedCharSequence> lines = font.split(FormattedText.of(text), notificationTextWidth);

            // Render the text using the GuiGraphics object
            guiGraphics.drawString(font, lines.get(0), x + 6, y + 6, color, false);
            guiGraphics.drawString(font, lines.get(1), x + 6, y + 16, color, false);

        }

        private void drawMiniNotifier(GuiGraphics guiGraphics, int x, int y)
        {
            //Draw the actual notifier
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, x, y, miniInfoReminderUVx, miniInfoReminderUVy, miniInfoWidthX, miniInfoHeightY, 512, 256);

            //Draw Small Key
            String keyName = "[" + Keybinds.notificationExpand.getKey().getDisplayName().getString() + "]";
            Font font = Minecraft.getInstance().font;
            int color = 0x522c58;

            //Get width of text
            int width = font.width(keyName);
            int miniXPlus = (miniInfoWidthX - width) / 2;

            //Get the scaling factor to know how much fits into the miniInfo box
            float scalingFactor = (float)miniInfoTextWidth / (float)width;
            scalingFactor = scalingFactor > 1 ? 1 : scalingFactor;

            //Do the scaling
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            Util.SafeScaleFromMiddle(poseStack, scalingFactor, scalingFactor,x + miniXPlus, y + 1, width, 6);
            guiGraphics.drawString(font, keyName, x + miniXPlus, y + 1, color, false);
            poseStack.popPose();
        }

        private void drawNotificationShadow(GuiGraphics guiGraphics, int x, int y)
        {
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, x, y + notificationHeighY, notificationUVx, notificationUVy + notificationHeighY, notificationWidthX, 2, 512, 256);

        }

        //Function that draws the big info box
        //To do so we have to:
        // - Calculate how many middle sections we need based on how much text we need to display
        // - Render Top
        // - Render the necessary amount of middle sections
        // - Render Bottom
        private void drawBigInfo(GuiGraphics guiGraphics, int x, int y)
        {
            //Calculate neededMiddleSections
            String text = Component.translatable(stackedNotifications.get(0).notificationLong).getString();
            Font font = Minecraft.getInstance().font;
            int color = 0x522c58;

            //Turn the text into two lines based on length
            List<FormattedCharSequence> lines = font.split(FormattedText.of(text), bigInfoTextWidth);
            int neededMiddleSections = lines.size();

            //Render Top
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, x, y, bigInfoTopUVx, bigInfoTopUVy, bigInfoTopWidthX, bigInfoTopHeightY, 512, 256);

            //Render Necessary amound of middles
            Util.drawRepatingBlit(guiGraphics, ReturnTicketWidget.TEXTURE, x, y + bigInfoTopHeightY, bigInfoMiddleUVx, bigInfoMiddleUVy, bigInfoTopWidthX, bigInfoMiddleHeightY, 512, 256, 1, neededMiddleSections);

            //Render Bottom
            guiGraphics.blit(ReturnTicketWidget.TEXTURE, x, y + bigInfoTopHeightY + (bigInfoMiddleHeightY * neededMiddleSections), bigInfoBottomUVx, bigInfoBottomUVy, bigInfoTopWidthX, bigInfoBottomHeightY, 512, 256);

            //Render Text
            for(int i = 0; i < neededMiddleSections; i++)
            {
                guiGraphics.drawString(font, lines.get(i), x + 3, y + 1 + bigInfoTopHeightY + (bigInfoMiddleHeightY * i), color, false);
            }


        }

        //Function that calculates the height that the big notifier has to take up to accomodate all of the different
        //Notifications that can exist
        //TODO: Actually implement this functionality
        private int bigInfoHeight()
        {
            //Calculate neededMiddleSections
            String text = Component.translatable(stackedNotifications.get(0).notificationLong).getString();
            Font font = Minecraft.getInstance().font;

            //Turn the text into two lines based on length
            List<FormattedCharSequence> lines = font.split(FormattedText.of(text), bigInfoTextWidth);
            int textLines = lines.size();

            return bigInfoTopHeightY + (bigInfoMiddleHeightY * textLines) + bigInfoBottomHeightY;
        }
    }

    //Adds a new notification
    //If another notification of the same type is already in the queue => Override that notification and discord all afterwards (-> Assumes status change)
    public static void addNotification(NotificationManager.CRTNotification newNotification)
    {
        //Goes through all notifications and search for duplicate
        int duplicateID = 0;
        boolean duplicatedDetected = false;
        for (int i = 0; i < stackedNotifications.size(); i++)
        {
            //If duplicate is detected
            if(stackedNotifications.get(i).notifcationShort.equals(newNotification.notifcationShort))
            {
                duplicateID = i;
                duplicatedDetected = true;
                break;
            }
        }

        //If we have a duplicate discard all after i
        if(duplicatedDetected)
        {
            if(duplicateID == 0)
            {
                stackedNotifications.clear();
            }

            else
            {
                stackedNotifications.subList(duplicateID, stackedNotifications.size()).clear();
            }
        }

        stackedNotifications.add(newNotification);
    }



}
