package com.qsmium.createreturnticket.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.mixins.InventoryMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ReturnTicketButton extends ImageButton
{

    private final Screen parent;
    private final Component TOOLTIP_TITLE;

    public ReturnTicketButton(int x, int y, OnPress pressAction, Player player, Screen parent) {
        super(x, y,   20, 20, 0, 0, 20, ReturnTicketWidget.TEXTURE, pressAction);
        this.parent = parent;
        this.TOOLTIP_TITLE = Component.literal("Cockckckkckc").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Minecraft.getInstance().player.isSpectator()) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

//    @Override
//    public void renderToolTip(PoseStack matrices, int mouseX, int mouseY) {
//        CurrencyTooltipRenderer.renderTooltip(
//                currencyStorage.getValue(),
//                matrices, parent,
//                TOOLTIP_TITLE,
//                x + 14, y + 5);
//    }
}