package com.qsmium.createreturnticket.mixins;

import com.qsmium.createreturnticket.gui.ReturnTicketButton;
import com.qsmium.createreturnticket.gui.ReturnTicketScreen;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = CreativeModeInventoryScreen.class, priority = 2000)
public class CreativeInventoryMixin extends Screen
{

    private ReturnTicketScreen m_returnTicketScreen;
    private ReturnTicketButton m_returnTicketButton;
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

        m_returnTicketButton = new ReturnTicketButton(self.getGuiLeft() + 130, self.getGuiTop() + 18, button -> {
            self.getMinecraft().setScreen(m_returnTicketScreen);
        }, self.getMinecraft().player, self);

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