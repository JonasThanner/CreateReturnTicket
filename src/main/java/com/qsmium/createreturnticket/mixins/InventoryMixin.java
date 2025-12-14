package com.qsmium.createreturnticket.mixins;

import com.qsmium.createreturnticket.gui.ReturnTicketScreen;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.qsmium.createreturnticket.gui.ReturnTicketWidget.TICKET_BUTTON;
import static com.qsmium.createreturnticket.gui.ReturnTicketWidget.TICKET_BUTTON_HOVER;

@Mixin(value = InventoryScreen.class, priority = 2000)
public abstract class InventoryMixin extends EffectRenderingInventoryScreen<InventoryMenu>
{
    //Mixin Initialization
    //We need to add our ReturnTicketScreen here
    public InventoryMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text)
    {
        super(screenHandler, playerInventory, text);
    }

    private ReturnTicketScreen returnTicketScreen;
    private ImageButton returnTicketButton;
    @Shadow
    private RecipeBookComponent recipeBookComponent;

    private boolean recipeBookVisiblePrevious = false;
    

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void init(CallbackInfo ci)
    {
        //Make new TicketScreen
        returnTicketScreen = new ReturnTicketScreen(this);

        //Check the ReturnTicket Status when opening the inventory
        // => We do this here also, so that there is hopefully no visual delay where the ticket pops up when opening the ticket screen
        ReturnTicketPacketHandler.requestTicketStatus();

        //Save the visibility of the recipe book
        recipeBookVisiblePrevious = recipeBookComponent.isVisible();

        WidgetSprites ticketButtonSprite = new WidgetSprites(TICKET_BUTTON, TICKET_BUTTON_HOVER);
        returnTicketButton = new ImageButton(this.leftPos + 140, this.topPos + 60, 20, 20, new WidgetSprites(TICKET_BUTTON, TICKET_BUTTON_HOVER), button -> {
            this.minecraft.setScreen(returnTicketScreen);
        });

        this.addRenderableWidget(returnTicketButton);
    }

    //At the end of the mouse click we check if we have to move our button
    @Inject(method = "mouseClicked", at = @At("TAIL"), cancellable = true, remap = true)
    public void onMouseClickedTail(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {
        //Handle recipe book changes
        // => If it changed on this mouse click, save the change and reinitialize the button
        boolean recipeBookChanged = false;
        if(recipeBookComponent.isVisible() && !recipeBookVisiblePrevious)
        {
            recipeBookChanged = true;
            recipeBookVisiblePrevious = true;
        }

        if(!recipeBookComponent.isVisible() && recipeBookVisiblePrevious)
        {
            recipeBookChanged = true;
            recipeBookVisiblePrevious = false;
        }

        //Set position to same position. leftPos and topPos should be adjusted now :3
        if(recipeBookChanged)
        {
            returnTicketButton.setPosition(this.leftPos + 140, this.topPos + 60);
        }
    }
}
