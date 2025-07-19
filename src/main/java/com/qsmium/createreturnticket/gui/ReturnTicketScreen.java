package com.qsmium.createreturnticket.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReturnTicketScreen extends Screen
{

    private ReturnTicketWindow returnTicketWidget;
    private ReturnTicketScreenCloseButton closeButton;

    private static final int RETURN_TICKET_WIDTH = 166;
    private static final int RETURN_TICKET_HEIGHT = 74;
    private static final int CLOSE_BUTTON_DISTANCE = 8;

    private Screen parentScreen;


    //Constructor for this screen
    // => This Screen gets build in the InventoryScreen
    public ReturnTicketScreen(Screen parentScreen)
    {
        super(Component.translatable("gui.createreturnticket.returnTicketScreen.title"));

        this.parentScreen = parentScreen;
    }

    //Called on screen Opening
    //we need to:
    // - Add the ReturnTicketWidget
    @Override
    public void init()
    {
        super.init();

        //The Actual Widget that houses the Return Ticket Image
        returnTicketWidget = new ReturnTicketWindow(this.width / 2 - (RETURN_TICKET_WIDTH / 2), this.height / 2 - (RETURN_TICKET_HEIGHT / 2), RETURN_TICKET_WIDTH, RETURN_TICKET_HEIGHT, minecraft);
        //addRenderableWidget(returnTicketWidget);

        //The Close button
        closeButton = new ReturnTicketScreenCloseButton(this.width - ReturnTicketScreenCloseButton.CLOSE_BUTTON_UV_WIDTH - CLOSE_BUTTON_DISTANCE, CLOSE_BUTTON_DISTANCE, this::backToInv, this);
        addRenderableWidget(closeButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // First, render the background
        // This gives the default dark transparent background
        this.renderBackground(guiGraphics);

        // Render the title text at the top of the screen
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        returnTicketWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        //Main Rendering Method => Will render all Widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isMouseOver(double x, double y)
    {
        closeButton.isMouseOver(x, y);

        return true;
    }

    //Back to inventory
    public void backToInv()
    {
        this.minecraft.setScreen(parentScreen);
    }

    //OnClose we want to close the entire screen and not return to the inventory
    // - Could be changed later
    @Override
    public void onClose()
    {
        this.minecraft.setScreen(null);
    }

    //We cant pause the game in this screen
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
