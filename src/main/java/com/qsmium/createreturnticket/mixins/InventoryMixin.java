package com.qsmium.createreturnticket.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.gui.ReturnTicketButton;
import com.qsmium.createreturnticket.gui.ReturnTicketWidget;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(InventoryScreen.class)
public abstract class InventoryMixin extends EffectRenderingInventoryScreen<InventoryMenu>
{
    public InventoryMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    private ReturnTicketButton returnTicketButton;
    private ReturnTicketWindow returnTicketWidget;
    @Shadow
    private RecipeBookComponent recipeBookComponent;

    private boolean recipeBookVisiblePrevious = false;


    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void init(CallbackInfo ci) {

        //Save the visibility of the recipe book
        recipeBookVisiblePrevious = recipeBookComponent.isVisible();

        //The Actual Widget that houses the Return Ticket Image
        returnTicketWidget = new ReturnTicketWindow(this.leftPos + 5, this.topPos + 6, 166, 74, minecraft);

        returnTicketButton = new ReturnTicketButton(this.leftPos + 140, this.topPos + 60, button -> {
            returnTicketWidget.toggleActive();
        }, minecraft.player, this);

        this.addRenderableWidget(returnTicketButton);
        //this.addWidget(returnTicketWidget);
    }

    @Inject(method = "render", at = @At("TAIL"), remap = true)
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        returnTicketWidget.render(graphics, mouseX, mouseY, delta);
    }

    //We have to catch mouse clicks and redirect them to our widget
    @Inject(method = "mouseClicked", at = @At("TAIL"), cancellable = true, remap = true)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (returnTicketWidget.mouseClicked(mouseX, mouseY, button))
        {
            cir.setReturnValue(true);
        }

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

    //Catch mouse release clicks
    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true, remap = true)
    public void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {
        if (returnTicketWidget.mouseReleased(mouseX, mouseY, button))
        {
            cir.setReturnValue(true);
        }
    }

    //Inject the update for our button into the if(recipieBook.mousePressed) which means this code
    //only gets called when the recipe book gets clicked on
    @Inject(method = "mouseClicked", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;mouseClicked(DDI)Z",
            ordinal = 0,
            shift = At.Shift.BY, by = 2
    ), cancellable = true, remap = true)
    private void onFirstRecipeBookClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {

    }

}
