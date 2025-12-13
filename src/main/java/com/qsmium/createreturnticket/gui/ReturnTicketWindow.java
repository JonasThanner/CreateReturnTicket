package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jline.reader.Widget;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class ReturnTicketWindow extends AbstractWidget implements Widget, GuiEventListener
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModMain.MODID,"textures/return_ticket.png");
    public static final ResourceLocation TEXTURE2 = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "textures/return_ticket_two.png");
    public static final ResourceLocation CLOSE_BUTTON = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "textures/ticket_small_close_button.png");
    public static final ResourceLocation CLOSE_BUTTON_HOVER = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "textures/ticket_small_close_button_hover.png");
    public static final int TEXTURE_2_WIDTH = 512;
    public static final int TEXTURE_2_HEIGHT = 256;

    private final Minecraft client;
    private int x;
    private int y;
    private int width;
    private int height;
    private ReturnTicketScreen parent;

    private final int noTicketGraphicUVx = 409;
    private final int noTicketGraphicUVy = 144;
    private final int noTicketGraphicWidth = 102;
    private final int noTicketGraphicHeight = 39;
    private final int noTicketGraphicOffsetX = 31;
    private final int noTicketGraphicOffsetY = 19;

    private final int eastereggUVx = 442;
    private final int eastereggUVy = 50;
    private final int eastereggWidth = 70;
    private final int eastereggHeight = 69;
    private final int eastereggOffsetX = 108;
    private final int eastereggOffsetY = -24;

    public static boolean activeTicket = false;
    //private final List<Button> buttons = new ArrayList<>();
    private boolean mousePressed = false;
    private int currentRipStage = 0;

    private static ReturnTicketWidget ticketWidget;
    private ImageButton closeButton;

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
    }

//    @SubscribeEvent
//    public static void onMouseDraggedEvent(ScreenEvent.MouseDragged event)
//    {
//        ticketWidget.mouseDragged(event.getDragX(), event.getDragY());
//    }

    public ReturnTicketWindow(int x, int y, int width, int height, Minecraft client, ReturnTicketScreen parent)
    {
        super(x, y, width, height, null);
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;

        //Add our TicketWidget
        ticketWidget = new ReturnTicketWidget(x + 32, y + 13, 110, 50, client);

        //Add Close Button
        WidgetSprites closeButtonSprites = new WidgetSprites(CLOSE_BUTTON, CLOSE_BUTTON_HOVER);

        closeButton = new ImageButton(x + 150, y + 15, 7, 7, closeButtonSprites, button -> {this.clickCloseButton();});

        NeoForge.EVENT_BUS.register(this);
    }

    //Class to update the x,y,width height etc.
    public void UpdateVariables(int x, int y, int width, int height, Minecraft client)
    {
        //Update our own variables
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        //Update the children
        // => Ticket Widget & Button
        closeButton.setX(x + 150);
        closeButton.setY(y + 15);
        ticketWidget.setX(x + 20);
        ticketWidget.setY(y + 12);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        PoseStack poseStack = graphics.pose();

        // Push the current pose to preserve the state
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);

        //Draw main texture of the window
        graphics.blit(TEXTURE, x, y, 0, 100, width, height, 512, 256);





        //Draw close button
        closeButton.renderWidget(graphics, mouseX, mouseY, delta);

        //Handle Ticket Rendering => Only if we have an active ticket
        if(activeTicket)
        {
            ticketWidget.renderWidget(graphics, mouseX, mouseY, delta);
        }

        //Draw Graphics if theres no active ticket
        else
        {
            graphics.blit(TEXTURE, x + noTicketGraphicOffsetX, y + noTicketGraphicOffsetY, noTicketGraphicUVx, noTicketGraphicUVy, noTicketGraphicWidth, noTicketGraphicHeight, 512, 256);
        }

        //Draw Easteregg Axolotl
        if(ReturnTicketWidget.INSTANCE.eastereggActive)
        {
            graphics.blit(TEXTURE, x + eastereggOffsetX, y + eastereggOffsetY, eastereggUVx, eastereggUVy, eastereggWidth, eastereggHeight, 512, 256);

            //Draw eyes
            if(!Util.isNight())
            {
                graphics.blit(TEXTURE, x + eastereggOffsetX + 18, y + eastereggOffsetY + 15, 470, 40, 25, 10, 512, 256);
            }
        }




        // Pop the pose to restore the previous state
        poseStack.popPose();




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


    //Override the mouseOver so that we can tell minecraft to also give us the mouseReleased when we rip the ticket
    @Override
    public boolean isMouseOver(double p_93672_, double p_93673_)
    {
        //If the ticket widget is focused we always catch all mouseOvers
        if(ticketWidget.isFocused())
        {
            return true;
        }

        return super.isMouseOver(p_93672_, p_93673_);
    }

    //Gets Called when the mouse is dragged
    @Override
    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_)
    {
        if(ticketWidget.isFocused())
        {
            return ticketWidget.mouseDragged(p_93645_, p_93646_, p_93647_, p_93648_, p_93649_);
        }
        //mousePressed = true;

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        //Either we release and the ticket widget handles it => We return the widget handler or we release inside then we also handle it
        if (ticketWidget.mouseReleased(mouseX, mouseY, button))
        {
            return true;
        }

        if (closeButton.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        //Either we click inside the window => We handle it
        //Or we click somewhere where the ticket handles it => return the ticket handling if it returns true
        if (ticketWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (closeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        //Check if we're clicking on the no ticket area axolotl
        if (Util.insideBoundsUI((int) mouseX, (int) mouseY, x + noTicketGraphicOffsetX + noTicketGraphicWidth - 65, y + noTicketGraphicOffsetY + noTicketGraphicHeight - 18, x + noTicketGraphicOffsetX + noTicketGraphicWidth, y + noTicketGraphicOffsetY + noTicketGraphicHeight)) {
            SoundUtils.playGlobalSound(SoundEvents.AXOLOTL_IDLE_AIR, 1.0f, Util.randomRange(0.8f, 1.2f));
        }

        return false;
    }

    @Override
    public boolean apply()
    {
        return false;
    }

    public void clickCloseButton()
    {
        parent.backToInv();
    }


}