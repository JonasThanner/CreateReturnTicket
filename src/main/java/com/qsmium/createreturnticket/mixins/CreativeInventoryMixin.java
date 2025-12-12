package com.qsmium.createreturnticket.mixins;

import com.qsmium.createreturnticket.gui.ReturnTicketScreen;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static com.qsmium.createreturnticket.gui.ReturnTicketWidget.TICKET_BUTTON;
import static com.qsmium.createreturnticket.gui.ReturnTicketWidget.TICKET_BUTTON_HOVER;


@Mixin(value = CreativeModeInventoryScreen.class, priority = 2000)
public class CreativeInventoryMixin extends Screen
{

    private ReturnTicketScreen m_returnTicketScreen;
    private ImageButton m_returnTicketButton;
    @Shadow
    private static CreativeModeTab selectedTab;

    protected CreativeInventoryMixin(Component p_96550_) {
        super(p_96550_);
    }

    //Literally just 1:1 Copy of the function in InventoryMixin. This not good but ok
    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void init(CallbackInfo ci)
    {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        m_returnTicketScreen = new ReturnTicketScreen(self);

        //Check the ReturnTicket Status when opening the inventory
        // => We do this here also, so that there is hopefully no visual delay where the ticket pops up when opening the ticket screen
        ReturnTicketPacketHandler.requestTicketStatus();

        WidgetSprites ticketButtonSprite = new WidgetSprites(TICKET_BUTTON, TICKET_BUTTON_HOVER);
        m_returnTicketButton = new ImageButton(self.getGuiLeft() + 130, self.getGuiTop() + 18, 20, 20, ticketButtonSprite, button -> {
            self.getMinecraft().setScreen(m_returnTicketScreen);
        });

        this.addRenderableWidget(m_returnTicketButton);

        //Set visibility in init
        m_returnTicketButton.visible = selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    //Set the button visible depending on what tab we are in right now
    @Inject(method = "selectTab", at = @At("TAIL"))
    public void selectTab(CreativeModeTab p_98561_, CallbackInfo ci)
    {
        if(m_returnTicketButton != null)
        {
            m_returnTicketButton.visible = selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
        }
    }
}