package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.ClientTicketDataHolder;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jline.reader.Widget;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ReturnTicketWidget extends AbstractWidget implements Widget, GuiEventListener
{

    public static ReturnTicketWidget INSTANCE;

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModMain.MODID,"textures/return_ticket.png");
    public static final ResourceLocation TICKET_BUTTON = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "ticket_inventory_button");
    public static final ResourceLocation TICKET_BUTTON_HOVER = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "ticket_inventory_button_hover");
    public static final int RETURN_TICKET_UV_WIDTH = 98;
    public static final int RETURN_TICKET_UV_HEIGHT = 50;
    public static final int RETURN_TICKT_UV_X = 20;
    public static final int RETURN_TICKET_UV_Y= 0;
    public static final int RIP_BACKGROUND_UV_WIDTH = 51;
    public static final int RIP_BACKGROUND_UV_HEIGHT = 50;
    public static final int BOX_UV_Y = 133;
    public static final int BOX_UV_HEIGHT = 17;
    public static final int BOX_UV_SIDES_WIDTH = 6;
    public static final int LEFT_BOX_UV_X = 257;
    public static final int RIGHT_BOX_UV_X = 295;
    public static final int MIDDLE_BOX_UV_X = 262;
    public static final int DIMENSION_UV_WH = 6;
    public static final int DIMENSION_UV_X = 0;
    public static final int DIMENSION_UV_Y = 174;
    private final Minecraft client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final int rippingAnimTime = 120;

    private boolean active = false;
    //private final List<Button> buttons = new ArrayList<>();
    private boolean mousePressed = false;
    private boolean rippingTicket = false;
    private boolean redeemingTicket = false;
    private int currentRedeemStage = 0;
    private int currentRipStage = 0;
    private int currentRippingAnimTimer = -1; //If its -1 => Disabled

    //Variables for when the ticket is ripped off
    // => Animations on redeeming
    private Vec2 mousePrev = new Vec2(0 ,0);
    private Vec2 mouseCur = new Vec2(0, 0);
    private Vec2 mouseVec = new Vec2(0, 0);
    private float rippedRotation = 0;
    private Vec2 redeemVec = new Vec2(0 , 0); //The that saves the mouse speed for the "floating away" anim
    private Vec2 redeemAnimVec = new Vec2(0, 0);
    private Vec2 redeemBaseVec = new Vec2(0, 0);
    private float currentRedeemAnimTime = -1;
    private final float redeemAnimTime = 10;
    private boolean redeemedTicked = false; // redeemedTicket != redeemingTicket

    //Easteregg Stuff
    private int timesClickedSqueak = 0;
    private int eastereggActivateCount = 10;
    public static boolean eastereggActive = false;
    private static int ticketWearTickCount = 100;
    private static boolean ticketAged = false;


    public ReturnTicketWidget(int x, int y, int width, int height, Minecraft client)
    {
        super(x, y, width, height, null);

        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        INSTANCE = this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        PoseStack poseStack = graphics.pose();

        //Calculate the rip stages only if the mouse is pressed otherwise we just leave
        //In this we calculate which of the two ripping things we activate
        //If none of the two are active => We choose one and dont let go of it until we release the mouse
        //If one is active => We treat that one
        if(mousePressed)
        {
            //If we are neither ripping nor redeeming => Choose which one to do
            if(!rippingTicket && !redeemingTicket)
            {
                //Decide in which areas we want to start the ripping and redeeming things

                //Case for ripping ticket
                if(mouseX >= 40 && mouseX <= x + 50 && mouseY >= y && mouseY <= y + 10)
                {
                    rippingTicket = true;
                }

                //Case for redeeming ticket
                else if (mouseX >= x + 80 && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
                {
                    redeemingTicket = true;
                }


            }

            //If we are doing one of the two things
            else
            {
                //If we are ripping the ticket
                if(rippingTicket)
                {
                    //Rip stage should be beginning from main x + main width - 31 and end at main x + main width + 50
                    int difference = height;
                    int mouseStartingAtTicket = mouseY - this.y;
                    int clampedMouseY = Util.Clamp(0, difference, mouseStartingAtTicket);
                    float diffRatio = (float)clampedMouseY / (float)difference;
                    currentRipStage = (int) Math.floor(diffRatio * 6);
                }

                //If we are redeeming the ticket
                if(redeemingTicket)
                {
                    //Rip stage should be beginning from main x + main width - 31 and end at main x + main width + 50
                    int difference = 31; //"Length" of the rip off width => Ie how far the mouse needs to move
                    int clampedMouseX = Util.Clamp(0, difference, mouseX - (x + width - 31));
                    float diffRatio = (float)clampedMouseX / (float)difference;
                    int newReddemStage = (int) Math.floor(diffRatio * 11);
                    currentRedeemStage = currentRedeemStage >= 11 ? 11 : newReddemStage;
                }
            }

        }

        //Graphics rendering incase we just ripped our ticket
        //If we ripped our ticket we want 2 Graphics
        //The left and right side of our graphics
        if(currentRippingAnimTimer >= 0)
        {
            //Increment Animation Time
            currentRippingAnimTimer++;

            //Render left side of ripped ticket
            //Left side should move down and to left
            int moveApart = (currentRippingAnimTimer) / 8;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, ((float)(120 - Util.Clamp(0, 120, (currentRippingAnimTimer * 2) - 120))) / 120.0f);

            float leftRippedX = ((float) currentRippingAnimTimer) / 25;
            float leftRippedY = ((float) currentRippingAnimTimer) / 16;
            //PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(leftRippedX, leftRippedY, 0);
            poseStack.rotateAround(new Quaternionf(0,0,0,0).setAngleAxis(-(float)currentRippingAnimTimer / 200.0f, 0, 0, 1), x + leftRippedX, y + leftRippedY, 0);
            graphics.blit(TEXTURE, this.x, this.y, 119, 0, 39, this.height, 512, 256);
            poseStack.popPose();

            //Render right side of ripped ticket
            //Side side should move upwards and to right
            float rightRippedX = ((float) currentRippingAnimTimer) / 32;
            float rightRippedY = ((float) currentRippingAnimTimer) / 16;

            poseStack.pushPose();
            poseStack.translate(rightRippedX, rightRippedY, 0);
            poseStack.rotateAround(new Quaternionf(0,0,0,0).setAngleAxis((float)currentRippingAnimTimer / 250.0f, 0, 0, 1), this.x + rightRippedX, this.y + rightRippedY, 0);
            graphics.blit(TEXTURE, this.x + 40, this.y, 186, 0, 58, this.height, 512, 256);
            poseStack.popPose();

            RenderSystem.disableBlend();



            //Exiting Animation
            if(currentRippingAnimTimer >= 120)
            {
                currentRippingAnimTimer = -1;
            }
        }

        //Draw the ticket if its not being ripped apart
        else
        {
            poseStack.pushPose();

            //=================== Ticket Ripping Animation Explanation ==================
            //For ripping the ticket we
            // - Draw a mask to hide the ticket where its "ripped" i.e has air
            // - Draw the main part of the ticket
            // - Draw the "backside" of the ticket
            // => Stencil Mask has to be done before whats being masked out
            //=================== Ticket Ripping Animation Explanation ==================
            Util.setupStencilMask();
            RenderSystem.setShaderTexture(0, ReturnTicketWindow.TEXTURE2);
            graphics.blit(ReturnTicketWindow.TEXTURE2, this.x, this.y, RIP_BACKGROUND_UV_WIDTH * (currentRipStage - 1), 72, RIP_BACKGROUND_UV_WIDTH, 46, 512, 256);

            //So its the correct Stencil Mode
            Util.setupStencilTexture(GL11.GL_NOTEQUAL);

            //I dont know why this line was here, but if its commented out theres constant OpenGL Errors but im leaving it here incase it was needed / useful and i just dont get the use right now
            //Util.setupStencilTexture();

            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderTexture(0, ReturnTicketWindow.TEXTURE2);


            //Draw the main Ticket
            // => If the Aged Ticket easteregg gets displayed, display that
            if(!ticketAged)
            {
                graphics.blit(TEXTURE, this.x, this.y, RETURN_TICKT_UV_X, RETURN_TICKET_UV_Y, RETURN_TICKET_UV_WIDTH - 19, RETURN_TICKET_UV_HEIGHT, 512, 256);
            }
            else
            {
                graphics.blit(TEXTURE, this.x, this.y, 248, 0, 80, this.height, 512, 256);
            }

            //Draw the Ticket Naming
            Font font = Minecraft.getInstance().font;
            int color = 0x918d8d;
            int color2 = 0xb987b4;

            //Turn the text into two lines based on length
            //List<FormattedCharSequence> lines = font.split(FormattedText.of(text), notificationTextWidth);

            //Render the Station Indicators
            poseStack.pushPose();
            float scale = 3.0f / 7.0f;
            int fontWidthEnterStation = font.width(ClientTicketDataHolder.enterStationDirectionIndicator);
            Util.SafeScale2(poseStack, scale, scale, x + 24, (float)y + 12f, fontWidthEnterStation, 7);
            graphics.drawString(font, ClientTicketDataHolder.enterStationDirectionIndicator, x + 24, y + 12, color2, false);
            poseStack.popPose();

            poseStack.pushPose();
            int fontWidthExitStation = font.width(ClientTicketDataHolder.exitStationDirectionIndicator);
            Util.SafeScale2(poseStack, scale, scale, x + 13, (float)y + 27f, fontWidthExitStation, 7);
            graphics.drawString(font, ClientTicketDataHolder.exitStationDirectionIndicator, x + 13, y + 27, color2, false);
            poseStack.popPose();

            //Render Text of Actual Station Names
            graphics.drawString(font, ClientTicketDataHolder.enterStation, x + 4, y + 16, color, false);
            graphics.drawString(font, ClientTicketDataHolder.exitStation, x + 4, y + 31, color, false);

            //Draw the Dimension of either exit or enter station
            //To draw Dimension we have to know the length of the exit/enter station direction indicator. To do so we multiply the font width with 0.45 and add 1px and the round back to int
            drawStationDimensions(graphics, ClientTicketDataHolder.enterStationDimension , (int) (x + 24 + ((float)fontWidthEnterStation * 0.45f)), y + 12);
            drawStationDimensions(graphics, ClientTicketDataHolder.exitStationDimension , (int) (x + 13 + ((float)fontWidthExitStation * 0.45f)), y + 27);

            //Draw the ripping ticket backside
            graphics.blit(ReturnTicketWindow.TEXTURE2, this.x, this.y, RIP_BACKGROUND_UV_WIDTH * (currentRipStage - 1), 0, RIP_BACKGROUND_UV_WIDTH, RIP_BACKGROUND_UV_HEIGHT, 512, 256);


            Util.disableStencil();
            poseStack.popPose();


            //Draw the actual part of the ticket that gets ripped off => Redeeming
            //If the ripped off part is still attached to the ticket it should stay in one place
            // => Once its ripped off, it should move with the mouse
            if(currentRedeemStage < 11)
            {
                graphics.blit(TEXTURE, this.x + 79, this.y, 31 * currentRedeemStage, this.height, 31, this.height, 512, 256);

            }

            //When the ticket is ripped off
            // => Calculate the vector of the mouse speed
            // => Change rotation based on the difference
            // => Draw the ticket with rotation
            else
            {
                //Ripped Anim Percentage
                float reddemAnimPercentage = 0;
                Vec2 basePosVec;



                if(!redeemedTicked)
                {
                    //Calculate vector of mouse speed
                    //Vec2 mouseVec = new Vec2(mouseX, mouseY);

                    //Calculate diff and adjust rotation based on that
                    float mouseXSpeed = (mouseVec.x) * 10;
                    rippedRotation = Util.Clamp(-30, 30, rippedRotation + ((1 - (Math.abs(rippedRotation) / 30)) * mouseXSpeed));

                    basePosVec = new Vec2(mouseX, mouseY);

                }
                else
                {
                    if(currentRedeemAnimTime < 0)
                    {
                        //TODO: DANGER ZONE OMG THIS SUCKS SO BAD
                        return;
                    }
                    currentRedeemAnimTime += delta;

                    //Incase the Animation is done
                    // => Then we stop the anim, i.e set redeemAnimTimer to -1
                    // => And we close the screen
                    // => And we send the pre redeem request to start the transit animation
                    if(currentRedeemAnimTime > redeemAnimTime)
                    {
                        currentRedeemAnimTime = -1;
                        client.setScreen(null);

                        //Send Pre-Redeem Request
                        //Everything gets handled by the PacketHandler from here on
                        ReturnTicketPacketHandler.preRedeemTicket();

                        //Return so we dont render the ripped off ticket for one frame
                        return;
                    }

                    //Calculate the float
                    reddemAnimPercentage = currentRedeemAnimTime / redeemAnimTime;

                    //Vec calculations
                    //Dampen moveSpeed
                    redeemAnimVec = new Vec2(redeemAnimVec.x + redeemVec.x, redeemAnimVec.y + redeemVec.y);
                    redeemVec = new Vec2(redeemVec.x * (1.0f - (0.1f * delta)), redeemVec.y * (1.0f - (0.1f * delta)));

                    basePosVec = redeemBaseVec;
                }



                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f - Util.Clamp(0.0f, 1.0f, reddemAnimPercentage));

                poseStack.pushPose();
                poseStack.translate(redeemAnimVec.x, redeemAnimVec.y, 0);
                poseStack.rotateAround(new Quaternionf(0,0,0,0).setAngleAxis(Math.toRadians(rippedRotation), 0, 0, 1), mouseX, mouseY, 0);
                graphics.blit(TEXTURE, (int) (basePosVec.x - 16), (int) (basePosVec.y - 25), 31 * currentRedeemStage, this.height, 31, this.height, 512, 256);
                poseStack.popPose();

                RenderSystem.disableBlend();

                //Ripped rotation dampening
                rippedRotation = rippedRotation * 0.9f;



            }

        }

        //Draw Coordinates of Stations
        //Needs to be done after everything because we want it to draw ontop
        if(currentRipStage == 0 && currentRedeemStage == 0)
        {
            drawStationCoordinatesTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_)
    {

    }

    public void mouseDragged(double dragX, double dragY)
    {
        mouseVec = new Vec2((float) dragX, (float) dragY);

    }

    //Gets Called when the mouse is dragged
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_93647_, double dragX, double dragY)
    {
        //Saving mouse position
        mousePrev = mouseCur;
        mouseCur = new Vec2((float) mouseX, (float) mouseY);

        //mouseVec = new Vec2((float) dragX, (float) dragY);
        //mousePressed = true;

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        //Defocus the ticket widget in all cases
        setFocused(false);

        if(active && mousePressed)
        {

            //Handle Ticket redemption
            //This is the actual part where the ticket gets redemeed
            if(currentRedeemStage >= 11 && !redeemedTicked)
            {
//                //Close inventory screen
//                Minecraft mc = Minecraft.getInstance();
//                if (mc.screen instanceof InventoryScreen) {
//                    mc.setScreen(null);  // Closes the screen
//                }

                //Activate redeeming animation
                redeemedTicked = true;
                currentRedeemAnimTime = 0;
                redeemBaseVec = new Vec2((float) mouseX, (float) mouseY);
                redeemVec = mouseVec;
            }

            //Handle Ticket Ripping
            if(currentRipStage >= 5)
            {
                //RIP TICKET
                currentRippingAnimTimer = 0;
                ClientTicketDataHolder.activeTicket = false;
                ReturnTicketPacketHandler.sendWorkToServer(ReturnTicketPacketHandler.ClientToServerWork.RIP_TICKET);
                ticketAged = false;

                ClientTicketDataHolder.enterStation = "-";
                ClientTicketDataHolder.exitStation = "-";
            }

            //Reset all Ripping Variables
            redeemingTicket = false;
            rippingTicket = false;

            currentRedeemStage = redeemedTicked ? currentRedeemStage : 0;
            currentRipStage = 0;

            mousePressed = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        //Easteregg Counter
        if(active && Util.insideBoundsUI((int) mouseX, (int) mouseY, x + 5, y + 43, x + 10, y + 47))
        {
            timesClickedSqueak++;

            //Play sound
            SoundUtils.playGlobalSound(SoundEvents.AXOLOTL_IDLE_AIR, 1.0f, Util.randomRange(0.8f, 1.2f));

            //Check Squeak Counter to activate easteregg
            if(timesClickedSqueak > eastereggActivateCount)
            {
                eastereggActive = true;
            }
        }


        if(isMouseOver(mouseX, mouseY) && active)
        {
            setFocused(true);
            mousePressed = true;
            return true;
        }

        return false;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        //Required to not draw tooltips for items in the crafting interface
        if(mouseX >= x && mouseX <= x + RETURN_TICKET_UV_WIDTH && mouseY >= y && mouseY <= y + RETURN_TICKET_UV_HEIGHT && active)
        {



            return true;
        }
        return false;
    }

    //To draw the station dimension we do the following:
    // - Check if the requested dimension has an associated ticket
    // - If it does:
    //      - Request the station uv x => We assume that in the texture all dim icons are in the same row and 6px long so they are just mapped to integers => x * 6 = UV_X
    //      - Draw the station and scale it down by 2x
    // - If it doesnt:
    //      - Draw the station dimension string after the : and uppercase it => minecraft:the_end => The End
    private void drawStationDimensions(GuiGraphics graphics, String dimension, int drawX, int drawY)
    {
        PoseStack pose = graphics.pose();

        //Check if the station dimension exists
        //
        //If the dimension has an icon
        int dimIcon = DimToIconOrText.DoesDimHaveIcon(dimension);
        if(dimIcon >= 0)
        {
            //Scale down by 3 / Dimension uv height/wdith
            float scale = 3.0f / ((float) DIMENSION_UV_WH);

            pose.pushPose();
            Util.SafeScale2(pose, scale, scale, drawX, drawY, DIMENSION_UV_WH, DIMENSION_UV_WH);

            //Draw the Icon at the x and y
            graphics.blit(TEXTURE, drawX, drawY, DIMENSION_UV_X + (DIMENSION_UV_WH * dimIcon), DIMENSION_UV_Y, DIMENSION_UV_WH, DIMENSION_UV_WH, 512, 256);

            pose.popPose();
        }

        //If the dimension doesnt have an icon
        else
        {
            //Make the dimension to the correct string
            // => get only the words after :
            Pair<String, String> split = Util.SeperateString(dimension, ":");
            String result = split.getSecond().replaceAll("_", " ").toUpperCase();

            //Render the Dimension text
            pose.pushPose();
            float scale = 3.0f / 7.0f;
            Font font = Minecraft.getInstance().font;
            Util.SafeScale2(pose, scale, scale, drawX, drawY, font.width(result), 7);
            graphics.drawString(font, result, drawX, drawY, 0xb987b4, false);
            pose.popPose();
        }
    }

    //To draw station tooltips, we first check if the mouse is over either the enter or exit location
    //If it is => Then we draw the actual coordinates
    // - To draw the coordinates, we first grab the coordinates, and then convert them to a minecraft string
    // - Then we calculate how long that text string is
    // - Based on this, we then first draw the left side of the box i.e like this [ => Box should stay attached to mouse
    // - then we draw the main part of the box and stretch it to the needed size
    private void drawStationCoordinatesTooltip(GuiGraphics graphics, double mouseX, double mouseY)
    {
        // Only show the tooltip when the mouse is over one of the station names.
        if (isMouseOverStationNames(mouseX, mouseY))
        {

            // Grab the station coordinates.
            //TODO: Grab real coordinates
            int coordX;
            int coordY;
            if(isOverEntranceStation(mouseX, mouseY))
            {
                if(!ClientTicketDataHolder.enterLocExists)
                {
                    return;
                }
                BlockPos pos = ClientTicketDataHolder.enterLocation;
                coordX = pos.getX();
                coordY = pos.getY();
            }
            else
            {
                if(!ClientTicketDataHolder.exitLocExists)
                {
                    return;
                }
                BlockPos pos = ClientTicketDataHolder.exitLocation;
                coordX = pos.getX();
                coordY = pos.getY();
            }

            // Convert the coordinates into a nicely formatted string.
            // For example: "X: 100 Y: 200"
            String coordinateString = "X: " + coordX + " Y: " + coordY;

            // Calculate the width (in pixels) of the coordinate string so that the box can scale.
            Font font = Minecraft.getInstance().font;
            int textWidth = font.width(coordinateString);

            // Determine where the tooltip box will be drawn; here we attach it to the current mouse position.
            //Additionally we need to add an offset here because its supposed to draw not directly at the mouse
            int tooltipX = (int) mouseX + 2;
            int tooltipY = (int) mouseY - BOX_UV_HEIGHT - 2;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // --- Draw the left side of the box ---
            // This part is drawn at the tooltip’s origin.
            graphics.blit(TEXTURE, tooltipX, tooltipY, LEFT_BOX_UV_X, BOX_UV_Y, BOX_UV_SIDES_WIDTH, BOX_UV_HEIGHT, 512, 256);

            //Scale the middle Part to the required text size
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            int middleX = tooltipX + BOX_UV_SIDES_WIDTH;
            Util.SafeScale2(poseStack, textWidth, 1, middleX, tooltipY, 10, BOX_UV_HEIGHT);

            // --- Draw the middle (stretchable) part of the box ---
            // The middle part is drawn immediately to the right of the left side.

            graphics.blit(TEXTURE, middleX, tooltipY, MIDDLE_BOX_UV_X, BOX_UV_Y, 1, BOX_UV_HEIGHT, 512, 256);


            poseStack.popPose();

            // --- Draw the right side of the box ---
            // The right side is drawn immediately after the middle part.
            int rightX = middleX + textWidth;
            graphics.blit(TEXTURE, rightX, tooltipY, RIGHT_BOX_UV_X, BOX_UV_Y, BOX_UV_SIDES_WIDTH, BOX_UV_HEIGHT, 512, 256);

            // --- Draw the coordinate string on top of the box ---
            // Here, the text is drawn within the middle section.
            // Adjust the vertical offset so that the text is centered.
            int textX = tooltipX + BOX_UV_SIDES_WIDTH;
            int textY = tooltipY + ((BOX_UV_HEIGHT - font.lineHeight) / 2) + 1;
            int color = 0xb987b4;
            graphics.drawString(font, coordinateString, textX, textY, color, false);

            RenderSystem.disableBlend();
        }
    }

    /**
     * Checks if the mouse is over the entrance station text.
     * Entrance station is drawn at (x + 4, y + 16) with a hard-coded bounding box size.
     */
    private boolean isOverEntranceStation(double mouseX, double mouseY) {
        // Starting position of the entrance station text.
        int stationX = x + 4;
        int stationY = y + 16;

        // Hard-coded bounds (magic numbers) for the bounding box.
        int boxWidth = 70;
        int boxHeight = 10;

        return Util.insideBoundsUI(
                (int) mouseX,
                (int) mouseY,
                stationX,
                stationY,
                stationX + boxWidth,
                stationY + boxHeight
        );
    }

    /**
     * Checks if the mouse is over the exit station text.
     * Exit station is drawn at (x + 4, y + 31) with a hard-coded bounding box size.
     */
    private boolean isOverExitStation(double mouseX, double mouseY) {
        // Starting position of the exit station text.
        int stationX = x + 4;
        int stationY = y + 31;

        // Hard-coded bounds (magic numbers) for the bounding box.
        int boxWidth = 80;
        int boxHeight = 10;

        return Util.insideBoundsUI(
                (int) mouseX,
                (int) mouseY,
                stationX,
                stationY,
                stationX + boxWidth,
                stationY + boxHeight
        );
    }

    /**
     * Checks if the mouse is over either the entrance or exit station texts.
     */
    private boolean isMouseOverStationNames(double mouseX, double mouseY) {
        return isOverEntranceStation(mouseX, mouseY) || isOverExitStation(mouseX, mouseY);
    }

    public void toggleActive() {
        active = !active;
        setActive(active);
    }

    public void setActive(boolean isActive)
    {
        //Reset some variables
        currentRedeemStage = 0;

        active = isActive;

        //Reset Easteregg Activation Counter
        if(!eastereggActive)
        {
            timesClickedSqueak = 0;
        }

        //setFocused(isActive);

        if(!active)
        {
            currentRippingAnimTimer = -1;
        }
    }

    @Override
    public boolean apply()
    {
        return false;
    }

    public static void setTicketAged(boolean isAged)
    {
        ticketAged = isAged;
    }

    public static void setTicketAged()
    {
        ticketAged = true;
    }

    public static void deleteTicket()
    {
        ticketAged = false;
    }


}