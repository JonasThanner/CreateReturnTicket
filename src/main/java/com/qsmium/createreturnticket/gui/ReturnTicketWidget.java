package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jline.reader.Widget;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class ReturnTicketWidget extends AbstractWidget implements Widget, GuiEventListener
{

    public static ReturnTicketWidget INSTANCE;

    public static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MODID,"textures/return_ticket.png");
    public static final int RETURN_TICKET_UV_WIDTH = 98;
    public static final int RETURN_TICKET_UV_HEIGHT = 50;
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







//        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 46, 24, 8, 37, 0, 16, TEXTURE, button -> {
//            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extractAll());
//            } else if (selectedValue() > 0) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extract(selectedValue()));
//                resetSelectedValue();
//            }
//        }));

    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        if (!active) return;




        PoseStack poseStack = graphics.pose();

        // Bind the texture
        //Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);



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
                    int difference = height * 2;
                    int clampedMouseY = Util.Clamp(0, height * 2, mouseY);
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
            //We only draw the easteregg aged ticket if the ticket isnt being ripped
            if(ticketAged && currentRipStage == 0)
            {
                graphics.blit(TEXTURE, this.x, this.y, 248, 0, 80, this.height, 512, 256);
            }
            else
            {
                //Draw the part of the ticket that gets ripped apart => Ripping of
                graphics.blit(TEXTURE, this.x, this.y, 51 * currentRipStage, 184, 51, this.height, 512, 256);

                //Draw the part of the ticket thats static
                graphics.blit(TEXTURE, this.x + 51, this.y, 20 + 51, 0, this.width - 31 - 51, this.height, 512, 256);

            }


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
                    if(currentRedeemAnimTime > redeemAnimTime)
                    {
                        currentRedeemAnimTime = -1;
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
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_)
    {

    }



//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (!this.active || client.player.isSpectator()) return false;
//
//        for (Button buttonWidget : buttons) {
//            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
//        }
//
//        return isMouseOver(mouseX, mouseY);
//    }

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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
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

                //Send Pre-Redeem Request
                //Everything gets handled by the PacketHandler from here on
                ReturnTicketPacketHandler.preRedeemTicket();

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
                ReturnTicketWindow.activeTicket = false;
                ticketAged = false;
            }

            //Reset all Ripping Variables
            redeemingTicket = false;
            rippingTicket = false;

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

    //Required to not draw tooltips for items in the crafting interface
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return mouseX >= x && mouseX <= x + RETURN_TICKET_UV_WIDTH && mouseY >= y && mouseY <= y + RETURN_TICKET_UV_HEIGHT && active;
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

        setFocused(isActive);

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