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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryMixin extends EffectRenderingInventoryScreen<InventoryMenu>
{
    public InventoryMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    private ReturnTicketButton returnTicketButton;
    private ReturnTicketWindow returnTicketWidget;

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    public void addButton(CallbackInfo ci) {
        //int purseX = ExampleClientConfig.CLIENT.pursePositionX.get();
        //int purseY = ExampleClientConfig.CLIENT.pursePositionY.get();

        //The Actual Widget that houses the Return Ticket Image
        returnTicketWidget = new ReturnTicketWindow(this.leftPos + 5, this.topPos + 5, 165, 74, minecraft);

        returnTicketButton = new ReturnTicketButton(this.leftPos + 140, this.topPos + 60, button -> {
            returnTicketWidget.toggleActive();
        }, minecraft.player, this);

        this.addRenderableWidget(returnTicketButton);
    }

    //Incredibly beautiful lambda mixin
//    @Inject(method = {"lambda$init$0","m_98879_"}, at = @At("TAIL"))
//    private void updateWidgetPosition(Button button, CallbackInfo ci) {
//        this.returnTicketButton.setPosition(this.leftPos + 158, this.topPos + 6);
//        this.numismatic$purse = new PurseWidget(this.leftPos + 129, this.topPos + 20, minecraft, CurrencyHolderAttacher.getExampleHolderUnwrap(minecraft.player));
//    }

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        returnTicketWidget.render(graphics, mouseX, mouseY, delta);
    }

    //We have to catch mouse clicks and redirect them to our widget
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (returnTicketWidget.mouseClicked(mouseX, mouseY, button))
        {
            cir.setReturnValue(true);
        }
    }

    //Catch mouse release clicks
    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true, remap = false)
    public void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (returnTicketWidget.mouseReleased(mouseX, mouseY, button))
        {
            cir.setReturnValue(true);
        }
    }

//    @Override
//    protected void renderTooltip(PoseStack matrices, int x, int y) {
//        if (numismatic$purse.isMouseOver(x, y)) return;
//        super.renderTooltip(matrices, x, y);
//    }

}
