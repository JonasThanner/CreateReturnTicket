package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.SoundUtils;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jline.reader.Widget;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ReturnTicketWindow extends AbstractWidget implements Widget, GuiEventListener
{
    public static ReturnTicketWindow instance;
    public static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MODID,"textures/return_ticket.png");
    public static final ResourceLocation TEXTURE2 = new ResourceLocation(ModMain.MODID, "textures/return_ticket_two.png");
    public static final int TEXTURE_2_WIDTH = 512;
    public static final int TEXTURE_2_HEIGHT = 256;

    public static final ResourceLocation TEST_MASK = new ResourceLocation(ModMain.MODID,"textures/test_mask.png");
    private final Minecraft client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final int noTicketGraphicUVx = 409;
    private final int noTicketGraphicUVy = 144;
    private final int noTicketGraphicWidth = 102;
    private final int noTicketGraphicHeight = 39;
    private final int noTicketGraphicOffsetX = 25;
    private final int noTicketGraphicOffsetY = 20;

    private final int eastereggUVx = 442;
    private final int eastereggUVy = 50;
    private final int eastereggWidth = 70;
    private final int eastereggHeight = 69;
    private final int eastereggOffsetX = 108;
    private final int eastereggOffsetY = -24;

    private boolean active = false;
    public static boolean activeTicket = false;
    //private final List<Button> buttons = new ArrayList<>();
    private boolean mousePressed = false;
    private int currentRipStage = 0;

    private ReturnTicketWidget ticketWidget;
    private ImageButton closeButton;

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
    }

    @SubscribeEvent
    public void onMouseDraggedEvent(ScreenEvent.MouseDragged event)
    {
        ticketWidget.mouseDragged(event.getDragX(), event.getDragY());
    }

//    @SubscribeEvent
//    public void onWorldTickEvent(TickEvent.World)
//    {
//
//    }


    public ReturnTicketWindow(int x, int y, int width, int height, Minecraft client)
    {
        super(x, y, width, height, null);
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        instance = this;



        //Add our TicketWidget
        ticketWidget = new ReturnTicketWidget(x + 20, y + 12, 110, 50, client);

        //Add Close Button
        closeButton = new ImageButton(x + 150, y + 15, 7, 7, 0, 40, 0, TEXTURE, 512, 256, button -> {closeWindow();});




//        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 46, 24, 8, 37, 0, 16, TEXTURE, button -> {
//            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extractAll());
//            } else if (selectedValue() > 0) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extract(selectedValue()));
//                resetSelectedValue();
//            }
//        }));

        MinecraftForge.EVENT_BUS.register(this);




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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(active)
        {
            //Either we release and the ticket widget handles it => We return the widget handler or we release inside then we also handle it
            if(ticketWidget.mouseReleased(mouseX, mouseY, button))
            {
                return true;
            }

            if(closeButton.mouseReleased(mouseX, mouseY, button))
            {
                return true;
            }

            return isMouseOver(mouseX, mouseY);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(active)
        {
            //Either we click inside the window => We handle it
            //Or we click somewhere where the ticket handles it => return the ticket handling if it returns true
            if(ticketWidget.mouseClicked(mouseX, mouseY, button))
            {
                return true;
            }

            if(closeButton.mouseClicked(mouseX, mouseY, button))
            {
                return true;
            }

            //Check if we're clicking on the no ticket area axolotl
            if(Util.insideBoundsUI((int) mouseX, (int) mouseY, x + noTicketGraphicOffsetX + noTicketGraphicWidth - 65, y + noTicketGraphicOffsetY + noTicketGraphicHeight - 18, x + noTicketGraphicOffsetX+ noTicketGraphicWidth, y + noTicketGraphicOffsetY + noTicketGraphicHeight))
            {
                SoundUtils.playGlobalSound(SoundEvents.AXOLOTL_IDLE_AIR, 1.0f, Util.randomRange(0.8f, 1.2f));
            }


            return isMouseOver(mouseX, mouseY);
        }

        return false;
    }

//    //Required to not draw tooltips for items in the crafting interface
//    @Override
//    public boolean isMouseOver(double mouseX, double mouseY) {
//        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
//    }

    public void toggleActive() {

        //Play Click sound
        SoundUtils.playGlobalSound(SoundEvents.UI_BUTTON_CLICK, 1.0f, 1.0f);

        ticketWidget.toggleActive();
        active = !active;
        setFocused(active);

        //Check new Ticket Status
        ReturnTicketPacketHandler.requestTicketStatus();
    }

    @Override
    public boolean apply()
    {
        return false;
    }

    public static void closeWindow()
    {
        instance.toggleActive();
    }


}