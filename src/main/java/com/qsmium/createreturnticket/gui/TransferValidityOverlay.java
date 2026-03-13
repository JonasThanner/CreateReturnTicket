package com.qsmium.createreturnticket.gui;

import com.qsmium.createreturnticket.ClientTicketDataHolder;
import com.qsmium.createreturnticket.ModMain;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class TransferValidityOverlay
{
    public static final ResourceLocation TEX_TRANSFER_VALID = ResourceLocation.fromNamespaceAndPath(ModMain.MODID,"textures/gui/overlay/transfer_validity_valid.png");
    public static final ResourceLocation TEX_TRANSFER_INVALID = ResourceLocation.fromNamespaceAndPath(ModMain.MODID,"textures/gui/overlay/transfer_validity_invalid.png");


    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "transfer_overlay"), TransferOverlayScreen::render);
    }

    public static class TransferOverlayScreen
    {
        private static final int VALIDITY_ICON_WIDTH = 19;
        private static final int VALIDITY_ICON_HEIGHT = 17;

        public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
        {
            int screenHeight = guiGraphics.guiHeight();
            int screenWidth = guiGraphics.guiWidth();

            //Draw Transfer Icon
            // Hotbar width and height (vanilla hotbar width is 182 pixels)
            int hotbarWidth = 182;

            // Calculate right side of hotbar
            int hotbarRightX = (screenWidth + hotbarWidth) / 2;

            // Calculate the position of the element you want to anchor to the hotbar
            int iconX = hotbarRightX + 2; //4 pixels from right
            int iconY = screenHeight - VALIDITY_ICON_HEIGHT - 2; //4 Pixels from bottom

            if(ClientTicketDataHolder.activeTicket)
            {
                if(ClientTicketDataHolder.transferValid)
                {
                    guiGraphics.blit(TEX_TRANSFER_VALID, iconX, iconY, 0, 0, VALIDITY_ICON_WIDTH, VALIDITY_ICON_HEIGHT, VALIDITY_ICON_WIDTH, VALIDITY_ICON_HEIGHT);
                }
                else
                {
                    guiGraphics.blit(TEX_TRANSFER_INVALID, iconX, iconY, 0, 0, VALIDITY_ICON_WIDTH, VALIDITY_ICON_HEIGHT, VALIDITY_ICON_WIDTH, VALIDITY_ICON_HEIGHT);
                }
            }
        }
    }
}
