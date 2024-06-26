package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.Util;
import com.qsmium.createreturnticket.mixins.InventoryMixin;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jline.reader.Widget;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ReturnTicketWidget extends AbstractWidget implements Widget, GuiEventListener
{

    public static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MODID,"textures/return_ticket.png");
    private final Minecraft client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private boolean active = false;
    //private final List<Button> buttons = new ArrayList<>();
    private boolean mousePressed = false;
    private int currentRipStage = 0;


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



        // Bind the texture
        //Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);

        // Draw the texture using blit
        graphics.blit(TEXTURE, this.x, this.y, 20, 0, this.width - 31, this.height);


        //Current Stage. Theres a total of 9 stages (including 0)
        int stage = 0;
        //Calculate the rip stage only if the mouse is pressed otherwise we just leave
        if(mousePressed)
        {
            //Rip stage should be beginning from main x + main width - 31 and end at main x + main width + 50
            int difference = 50 + 31;
            int clampedMouseX = Util.Clamp(0, 50 + 31, mouseX - (x + width - 31));
            float diffRatio = (float)clampedMouseX / (float)difference;
            stage = (int) Math.floor(diffRatio * 9);
            currentRipStage = stage;
        }



        //Draw the actual part of the ticket that gets ripped off
        graphics.blit(TEXTURE, this.x + 79, this.y, 31 * stage, this.height, 31, this.height);
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
            if(currentRipStage >= 8)
            {
                ReturnTicketPacketHandler.sendRedeem();
                currentRipStage = 0;
            }
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

//    //Required to not draw tooltips for items in the crafting interface
//    @Override
//    public boolean isMouseOver(double mouseX, double mouseY) {
//        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
//    }

    public void toggleActive() {
        active = !active;
        setFocused(active);
    }

    @Override
    public boolean apply()
    {
        return false;
    }


}