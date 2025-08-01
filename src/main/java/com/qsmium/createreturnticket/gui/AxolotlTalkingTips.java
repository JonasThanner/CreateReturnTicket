package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ClientTicketDataHolder;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec2;
import org.jline.reader.Widget;

import java.util.List;

public class AxolotlTalkingTips extends AbstractWidget implements Widget
{
    //In this class we render the small bubble that you can click to get tips from the axolotl in the ticket screen
    //We then also render the speech bubble that the axolotl displays and the tips from the axolotl
    // => Just a bool that saves if we show the hint or the full bubble + a timer how long the bubble has shown
    //From there we pick 1 from x number of hints that we display
    //The bubble gets scaled with the length that it needs to be to display the text -> If its longer than a set int, we scale the text
    //When we display the speech bubble, we set a timer. After the timer is up, we go back to the hint bubble text
    //
    //We also want to move the hint bubble every now and then to make it clear that you can click it
    //


    private static final int AXO_BUBBLE_HINT_UV_X = 400;
    private static final int AXO_BUBBLE_HINT_UV_Y = 107;
    private static final int AXO_BUBBLE_HINT_UV_WH = 7;
    private static final int AXO_BUBBLE_LEFT_UV_X = 481;
    private static final int AXO_BUBBLE_LEFT_UV_Y = 119;
    private static final int AXO_BUBBLE_LEFT_UV_WIDTH = 6;
    private static final int AXO_BUBBLE_LEFT_UV_HEIGHT = 19;
    private static final int AXO_BUBBLE_MIDDLE_WIDTH = 1;
    private static final int AXO_BUBBLE_MIDDLE_HEIGHT = 15;

    private static final int AXO_BUBBLE_TEXT_COLOR = 0x522c58;

    private static final int BIG_BUBBLE_DISPLAY_TIME = 200;
    private static final int TIP_NUMBERS = 2;
    private static final int HINT_HITBOX_LARGER = 5;
    private int BUBBLE_X;
    private int BUBBLE_Y;

    private int displayState = 0; //0 -> Display hint, 2 -> Display bubble. The intermediates are there incase an animation gets added in the future
    private int bigBubbleDisplayCurrentTimer = 0;
    private int currentTip = 0;
    private int currentBubbleLength = AXO_BUBBLE_HINT_UV_WH;
    private boolean clickedDown = false;



    public AxolotlTalkingTips(int x, int y, int width, int height, Minecraft client)
    {
        super(x, y, AXO_BUBBLE_HINT_UV_WH, AXO_BUBBLE_HINT_UV_WH, null);
        BUBBLE_X = x;
        BUBBLE_Y = y - AXO_BUBBLE_LEFT_UV_HEIGHT + AXO_BUBBLE_HINT_UV_WH;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        //Check the current animation time
        // => If the biBubbleDisplayCurrentTimer is over the  BIG_BUBBLE_DISPLAY_TIME -> Revert to displaystate 1
        if(bigBubbleDisplayCurrentTimer >= BIG_BUBBLE_DISPLAY_TIME)
        {
            bigBubbleDisplayCurrentTimer = 0;
            displayState = 0;
        }

        //Different behavior in the renderWidget depending on what we are rendering
        // - If we are in displayState = 0 we just render the little hint display
        switch (displayState)
        {
            //If we just render the little hint display
            case 0:

                //Render the hint display
                guiGraphics.blit(ReturnTicketWindow.TEXTURE, getX(), getY(), AXO_BUBBLE_HINT_UV_X, AXO_BUBBLE_HINT_UV_Y, AXO_BUBBLE_HINT_UV_WH, AXO_BUBBLE_HINT_UV_WH, 512, 256);


                break;

            //If we render the entire bubble we need to
            // - Increase anim time
            // - Get the tip text and check how long its going to be
            // - Render the left portion
            // - Render the middle and scale it up by the char size => Should be 1:1 since the middle is 1px wide
            // - Render the right side
            // - Render the text
            case 2:

                //Increase Anim time
                bigBubbleDisplayCurrentTimer++;

                //Get the tip text
                String tipText = Component.translatable("createreturnticket.axolotltip." + Integer.toString(currentTip)).getString();

                //Check the tip text length => How wide does it need to be
                Font font = Minecraft.getInstance().font;
                int neededPixels = font.width(tipText);
                currentBubbleLength = AXO_BUBBLE_LEFT_UV_WIDTH + AXO_BUBBLE_LEFT_UV_WIDTH + neededPixels;

                //Render left portion
                guiGraphics.blit(ReturnTicketWidget.TEXTURE, BUBBLE_X, BUBBLE_Y, AXO_BUBBLE_LEFT_UV_X, AXO_BUBBLE_LEFT_UV_Y, AXO_BUBBLE_LEFT_UV_WIDTH, AXO_BUBBLE_LEFT_UV_HEIGHT, 512, 256);

                //Render Middle
                //!! ONLY WORKS IF WE ASSUME MIDDLE WIDTH = 1 PX
                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                Util.SafeScale2(pose, neededPixels, 1, BUBBLE_X + AXO_BUBBLE_LEFT_UV_WIDTH, BUBBLE_Y, 512, 256);
                guiGraphics.blit(ReturnTicketWidget.TEXTURE, BUBBLE_X + AXO_BUBBLE_LEFT_UV_WIDTH, BUBBLE_Y, AXO_BUBBLE_LEFT_UV_X + AXO_BUBBLE_LEFT_UV_WIDTH, AXO_BUBBLE_LEFT_UV_Y, AXO_BUBBLE_MIDDLE_WIDTH, AXO_BUBBLE_MIDDLE_HEIGHT, 512, 256);
                pose.popPose();

                //Render Right Side
                guiGraphics.blit(ReturnTicketWidget.TEXTURE, BUBBLE_X + AXO_BUBBLE_LEFT_UV_WIDTH + neededPixels, BUBBLE_Y, AXO_BUBBLE_LEFT_UV_X + AXO_BUBBLE_LEFT_UV_WIDTH + 1, AXO_BUBBLE_LEFT_UV_Y, AXO_BUBBLE_LEFT_UV_WIDTH, AXO_BUBBLE_MIDDLE_HEIGHT, 512, 256);

                //Render Text
                guiGraphics.drawString(font, tipText, BUBBLE_X + 5, BUBBLE_Y + 4, AXO_BUBBLE_TEXT_COLOR, false);

                break;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_93647_, double dragX, double dragY)
    {
        return mouseDragged(mouseX, mouseY, p_93647_, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        //When we release the mouse and are over the widget, we:
        // - Reset the bigBubbleDisplayTimer
        // - Set display state to 2
        // - Iterate currenTip
        if(isMouseOver(mouseX, mouseY) && clickedDown)
        {
            clickedDown = false;
            bigBubbleDisplayCurrentTimer = 0;
            displayState = 2;

            //Loop back to first tip when we went trough all
            currentTip = (currentTip + 1) % TIP_NUMBERS;

            return true;
        }

        clickedDown = false;
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            clickedDown = true;

            super.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    //We have different isMouseOver depending on if we have the small hint bubble or the large bubble
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        //mouseOver check incase we have the small hint
        if(displayState == 0)
        {
            //Make the hitbox larger by a certain amount
            if(mouseX >= getX() - HINT_HITBOX_LARGER && mouseX <= getX() + AXO_BUBBLE_HINT_UV_WH + HINT_HITBOX_LARGER && mouseY >= getY() - HINT_HITBOX_LARGER && mouseY <= getY() + AXO_BUBBLE_HINT_UV_WH + HINT_HITBOX_LARGER)
            {
                return true;
            }

            return false;
        }

        //mouseOver check incase we have the large bubble
        else if (displayState == 2)
        {
            if(mouseX >= BUBBLE_X && mouseX <= BUBBLE_X + currentBubbleLength && mouseY >= BUBBLE_Y && mouseY <= BUBBLE_Y + AXO_BUBBLE_LEFT_UV_HEIGHT)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean apply()
    {
        return false;
    }
}
