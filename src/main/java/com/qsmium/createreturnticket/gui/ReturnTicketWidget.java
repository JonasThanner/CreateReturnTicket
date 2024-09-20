package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.TicketManager;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jline.reader.Widget;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class ReturnTicketWidget extends AbstractWidget implements Widget, GuiEventListener
{

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


    public ReturnTicketWidget(int x, int y, int width, int height, Minecraft client)
    {
        super(x, y, width, height, null);
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;





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
                    int difference = 50 + 31;
                    int clampedMouseX = Util.Clamp(0, 50 + 31, mouseX - (x + width - 31));
                    float diffRatio = (float)clampedMouseX / (float)difference;
                    currentRedeemStage = (int) Math.floor(diffRatio * 9);
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
            //Draw the part of the ticket that gets ripped apart => Ripping of
            graphics.blit(TEXTURE, this.x, this.y, 51 * currentRipStage, 184, 51, this.height, 512, 256);

            //Draw the part of the ticket thats static
            graphics.blit(TEXTURE, this.x + 51, this.y, 20 + 51, 0, this.width - 31 - 51, this.height, 512, 256);

            //Draw the actual part of the ticket that gets ripped off => Redeeming
            graphics.blit(TEXTURE, this.x + 79, this.y, 31 * currentRedeemStage, this.height, 31, this.height, 512, 256);

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

    //Gets Called when the mouse is dragged
    @Override
    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_)
    {
        //mousePressed = true;

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(active && mousePressed)
        {

            //Handle Ticket redemption
            if(currentRedeemStage >= 8)
            {
                ReturnTicketPacketHandler.sendRedeem();
            }

            //Handle Ticket Ripping
            if(currentRipStage >= 5)
            {
                //RIP TICKET
                currentRippingAnimTimer = 0;
                ReturnTicketWindow.activeTicket = false;
            }

            //Reset all Ripping Variables
            redeemingTicket = false;
            rippingTicket = false;
            currentRedeemStage = 0;
            currentRipStage = 0;

            mousePressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(isMouseOver(mouseX, mouseY) && active)
        {
            mousePressed = true;
            return true;
        }

        return false;
    }

    //Required to not draw tooltips for items in the crafting interface
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + RETURN_TICKET_UV_WIDTH && mouseY >= y && mouseY <= y + RETURN_TICKET_UV_HEIGHT && active;
    }

    public void toggleActive() {
        active = !active;
        setFocused(active);

        //Reset ripping animation on disable
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


}