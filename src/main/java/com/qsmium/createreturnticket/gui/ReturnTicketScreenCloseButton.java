package com.qsmium.createreturnticket.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import javax.swing.*;

public class ReturnTicketScreenCloseButton extends AbstractWidget
{

    private final Screen parent;
    private final Component TOOLTIP_TITLE;

    public static final int CLOSE_BUTTON_UV_WIDTH = 16;
    public static final int CLOSE_BUTTON_UV_X = 433;
    public static final int CLOSE_BUTTON_UV_Y = 119;

    private Runnable pressAction;
    private int offset = 0; //Offsets the UV by button width to get the other things

    public ReturnTicketScreenCloseButton(int x, int y, Runnable pressAction, Screen parent)
    {
        super(x, y, CLOSE_BUTTON_UV_WIDTH, CLOSE_BUTTON_UV_WIDTH, null);
        this.pressAction = pressAction;
        this.parent = parent;
        this.TOOLTIP_TITLE = Component.literal("Cockckckkckc").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        //If we click on the mouse we have the pressed down image
        if(isMouseOver(mouseX, mouseY))
        {
            offset = 2;
            return true;
        }

        //Just here for safety
        offset = 0;

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        //If clicked on the button
        // => Fullfill pressAction
        if(isMouseOver(mouseX, mouseY))
        {
            pressAction.run();
            return true;
        }

        //If we dont click on the button, we change back to the normal button image
        offset = 0;

        return false;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        //Check if we change texture
         //If we mouse over the button we have a different color
        if(mouseX >= this.getX() && mouseX <= this.getX() + CLOSE_BUTTON_UV_WIDTH && mouseY >= this.getY() && mouseY <= this.getY() + CLOSE_BUTTON_UV_WIDTH && offset != 2)
        {
            offset = 1;
        }
        else
        {
            if(offset != 2)
            {
                //If we go back to not hovering over go to normal image
                offset = 0;
            }
        }



        graphics.blit(ReturnTicketWidget.TEXTURE, this.getX(), this.getY(), CLOSE_BUTTON_UV_X + (CLOSE_BUTTON_UV_WIDTH * offset), CLOSE_BUTTON_UV_Y, CLOSE_BUTTON_UV_WIDTH, CLOSE_BUTTON_UV_WIDTH, 512, 256);
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_)
    {

    }
}