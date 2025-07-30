package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.opengl.GL11;

public class ReturnTicketScreen extends Screen
{
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
    private static final int TALKING_AXO_UV_X = 391;
    private static final int TALKING_AXO_UV_Y = 86;
    private static final int TALKING_AXO_UV_WIDTH = 42;
    private static final int TALKING_AXO_UV_HEIGHT = 18;


    private Screen parentScreen;
    private ReturnTicketWindow returnTicketWindow;
    private ReturnTicketScreenCloseButton closeButton;
    private AxolotlTalkingTips talkingTips;


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
        returnTicketWindow = new ReturnTicketWindow(this.width / 2 - (RETURN_TICKET_WIDTH / 2), this.height / 2 - (RETURN_TICKET_HEIGHT / 2), RETURN_TICKET_WIDTH, RETURN_TICKET_HEIGHT, minecraft, this);
        addRenderableWidget(returnTicketWindow);

        //The Close button
        closeButton = new ReturnTicketScreenCloseButton(this.width - ReturnTicketScreenCloseButton.CLOSE_BUTTON_UV_WIDTH - CLOSE_BUTTON_DISTANCE, CLOSE_BUTTON_DISTANCE, this::backToInv, this);
        addRenderableWidget(closeButton);

        //The tip button
        talkingTips = new AxolotlTalkingTips(returnTicketWindow.getX() + 51, returnTicketWindow.getY() - 16, 1, 1, minecraft);
        addWidget(talkingTips);

        //Check the Ticket status once again just to make sure
        // => Shouldve already been check when opening the inventory but it could maybe in some weird circumstances have changed
        ReturnTicketPacketHandler.requestTicketStatus();
    }

    @Override
    public void resize(Minecraft p_96575_, int p_96576_, int p_96577_)
    {
        super.resize(p_96575_, p_96576_, p_96577_);

        //Update ReturnTicketWindow with new variables
        // => Need to do this, because somehow the inventory init() gets called multiple times. No idea why but it does
        returnTicketWindow.UpdateVariables(this.width / 2 - (RETURN_TICKET_WIDTH / 2), this.height / 2 - (RETURN_TICKET_HEIGHT / 2), RETURN_TICKET_WIDTH, RETURN_TICKET_HEIGHT, minecraft);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // First, render the background
        // This gives the default dark transparent background
        this.renderBackground(guiGraphics);

        //Render the Return Ticket Logo at the top middle of the screen
        //Pose Stack push/pop is just for safety
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        guiGraphics.blit(ReturnTicketWindow.TEXTURE2, this.width / 2 - (LOGO_UV_WIDTH / 2), 10, LOGO_UV_X, LOGO_UV_Y, LOGO_UV_WIDTH, LOGO_UV_HEIGHT, ReturnTicketWindow.TEXTURE_2_WIDTH, ReturnTicketWindow.TEXTURE_2_HEIGHT);
        poseStack.popPose();

        //returnTicketWindow.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        //Main Rendering Method => Will render all Widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        //Flush depth buffer so we *DEFO* render ontop later
        guiGraphics.flush();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        //We need to render the axolotl and its tip seperately to make sure we have the correct ordering
        // => Render Axolotl first
        poseStack.pushPose();
        //Make sure we render behind the taling axolotl
        poseStack.translate(0, 0, 1);
        guiGraphics.blit(ReturnTicketWindow.TEXTURE, returnTicketWindow.getX() + 15, returnTicketWindow.getY() - 14, TALKING_AXO_UV_X, TALKING_AXO_UV_Y, TALKING_AXO_UV_WIDTH, TALKING_AXO_UV_HEIGHT, 512, 256);
        talkingTips.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        poseStack.popPose();


    }

    //Back to inventory
    // - Play the Button Click sound -> The only way we can go back is via a button click so we always play that
    public void backToInv()
    {
        SoundUtils.playGlobalSound(SoundEvents.UI_BUTTON_CLICK, 0.25f, 1.0f);

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
