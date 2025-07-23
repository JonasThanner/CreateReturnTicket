package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ReturnTicketScreen extends Screen
{

    private ReturnTicketWindow returnTicketWindow;
    private ReturnTicketScreenCloseButton closeButton;

    public static final ResourceLocation TEXTURE_THREE = new ResourceLocation(ModMain.MODID,"textures/return_ticket_three.png");
    private static final int TEXTURE_THREE_WIDTH = 256;
    private static final int TEXTURE_THREE_HEIGHT = 256;
    private static final int RETURN_TICKET_WIDTH = 166;
    private static final int RETURN_TICKET_HEIGHT = 74;
    private static final int CLOSE_BUTTON_DISTANCE = 8;
    private static final int LOGO_UV_X = 127;
    private static final int LOGO_UV_Y = 119;
    private static final int LOGO_UV_WIDTH = 113;
    private static final int LOGO_UV_HEIGHT = 57;

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
        returnTicketWindow = new ReturnTicketWindow(this.width / 2 - (RETURN_TICKET_WIDTH / 2), this.height / 2 - (RETURN_TICKET_HEIGHT / 2), RETURN_TICKET_WIDTH, RETURN_TICKET_HEIGHT, minecraft);
        //addWidget(returnTicketWindow);

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

        //Render the Return Ticket Logo at the top left of the screen
        //Pose Stack push/pop is just for safety
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        guiGraphics.blit(ReturnTicketWindow.TEXTURE2, this.width / 2 - (LOGO_UV_WIDTH / 2), 10, LOGO_UV_X, LOGO_UV_Y, LOGO_UV_WIDTH, LOGO_UV_HEIGHT, ReturnTicketWindow.TEXTURE_2_WIDTH, ReturnTicketWindow.TEXTURE_2_HEIGHT);
        poseStack.popPose();

        returnTicketWindow.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        //Main Rendering Method => Will render all Widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void onToggleScreen()
    {
        //Toggle whenever we open the screen
        returnTicketWindow.toggleActive();
    }

    public void onOpenScreen()
    {
        onToggleScreen();
    }

    @Override
    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_)
    {
        if(returnTicketWindow.mouseDragged(p_93645_, p_93646_, p_93647_, p_93648_, p_93649_))
        {
            return true;
        }

        return super.mouseDragged(p_93645_, p_93646_, p_93647_, p_93648_, p_93649_);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(returnTicketWindow.mouseReleased(mouseX, mouseY, button))
        {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(returnTicketWindow.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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
        onToggleScreen();

        this.minecraft.setScreen(null);
    }

    //We cant pause the game in this screen
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
