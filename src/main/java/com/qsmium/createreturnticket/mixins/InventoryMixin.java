package com.qsmium.createreturnticket.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.gui.ReturnTicketButton;
import com.qsmium.createreturnticket.gui.ReturnTicketScreen;
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
    //Mixin Initialization
    //We need to add our ReturnTicketScreen here
    public InventoryMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text)
    {
        super(screenHandler, playerInventory, text);

        //Create Return TIcket Screen
        //WHY DOES THIS NOT WORK?! => Need to reassign in init otherwise its null ÒwÓ
        returnTicketScreen = new ReturnTicketScreen(this);
    }

    private ReturnTicketScreen returnTicketScreen;
    private ReturnTicketButton returnTicketButton;
    @Shadow
    private RecipeBookComponent recipeBookComponent;

    private boolean recipeBookVisiblePrevious = false;
    

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void init(CallbackInfo ci)
    {
        //Init ReturnTicketScreen
        returnTicketScreen = new ReturnTicketScreen(this);
        returnTicketScreen.init();

        //Save the visibility of the recipe book
        recipeBookVisiblePrevious = recipeBookComponent.isVisible();

        returnTicketButton = new ReturnTicketButton(this.leftPos + 140, this.topPos + 60, button -> {
            returnTicketScreen.onOpenScreen();
            this.minecraft.setScreen(returnTicketScreen);
        }, minecraft.player, this);

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
