package com.qsmium.createreturnticket.gui;

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
public class AxolotlOverlay
{
    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "axolotl_overlay"), AxolotlOverlay.AxolotlOverlayScreen::render);
    }

    public static class AxolotlOverlayScreen
    {
        private static final int axolotlSleepingUVx = 284;
        private static final int axolotlSleepingUVy = 169;
        private static final int axolotlSleepingWidth = 48;
        private static final int axolotlSleepingHeight = 15;

        private static final int axolotlHotbarUVx = 332;
        private static final int axolotlHotbarUVy = 169;
        private static final int axolotlHotbarWidth = 40;
        private static final int axolotlHotbarHeight = 15;

        private static final int axolotlBrownUVx = 373;
        private static final int axolotlBrownUVy = 166;
        private static final int axolotlBrownWidth = 35;
        private static final int axolotlBrownHeight = 18;

        private static final int axolotlPairUVx = 366;
        private static final int axolotlPairUVy = 115;
        private static final int axolotlPairWidth = 42;
        private static final int axolotlPairHeight = 51;

        private static final int axolotlYellowUVx = 408;
        private static final int axolotlYellowUVy = 104;
        private static final int axolotlYellowWidth = 25;
        private static final int axolotlYellowHeight = 39;

        public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
        {
            int screenHeight = guiGraphics.guiHeight();
            int screenWidth = guiGraphics.guiWidth();

            if(ReturnTicketWidget.eastereggActive)
            {
                //Draw Axolotl sleeping on bottom right side
                guiGraphics.blit(ReturnTicketWindow.TEXTURE, (screenWidth / 4) * 3, screenHeight - axolotlSleepingHeight, axolotlSleepingUVx, axolotlSleepingUVy, axolotlSleepingWidth, axolotlSleepingHeight, 512, 256);

                //Draw brown axolotl hanging from top
                guiGraphics.blit(ReturnTicketWindow.TEXTURE, (screenWidth / 4) * 1, 0, axolotlBrownUVx, axolotlBrownUVy, axolotlBrownWidth, axolotlBrownHeight, 512, 256);

                //Draw axolotl pair hanging from top
                guiGraphics.blit(ReturnTicketWindow.TEXTURE, (screenWidth / 8) * 7, 0, axolotlPairUVx, axolotlPairUVy, axolotlPairWidth, axolotlPairHeight, 512, 256);

                //Draw yellow axolotl on side
                guiGraphics.blit(ReturnTicketWindow.TEXTURE, screenWidth - axolotlYellowWidth, (screenHeight / 3), axolotlYellowUVx, axolotlYellowUVy, axolotlYellowWidth, axolotlYellowHeight, 512, 256);

                //Draw Axoltl thats sitting on the hotbar
                // Hotbar width and height (vanilla hotbar width is 182 pixels)
                int hotbarWidth = 182;
                int hotbarHeight = 22;

                // Calculate center of the screen for hotbar position
                int hotbarX = (screenWidth - hotbarWidth) / 2;
                int hotbarY = screenHeight - hotbarHeight - 5;  // 5 pixels above the bottom edge

                // Define the offset where your element should be rendered relative to the hotbar
                int offsetX = 0;  // Example offset: adjust this to move the drawing horizontally
                int offsetY = -6; // Example offset: adjust this to move the drawing vertically above the hotbar

                // Calculate the position of the element you want to anchor to the hotbar
                int axolotlX = hotbarX + offsetX;
                int axolotlY = hotbarY + offsetY;

                guiGraphics.blit(ReturnTicketWindow.TEXTURE, axolotlX, axolotlY, axolotlHotbarUVx, axolotlHotbarUVy, axolotlHotbarWidth, axolotlHotbarHeight, 512, 256);

            }

        }
    }
}
