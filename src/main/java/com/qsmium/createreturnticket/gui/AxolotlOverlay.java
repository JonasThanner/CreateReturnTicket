package com.qsmium.createreturnticket.gui;

import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AxolotlOverlay
{

    @SubscribeEvent
    public static void onOverlayRegister(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("axolotl_overlay", new AxolotlOverlay.AxolotlOverlayScreen());
    }

    private static class AxolotlOverlayScreen implements IGuiOverlay
    {

        private final int axolotlSleepingUVx = 284;
        private final int axolotlSleepingUVy = 169;
        private final int axolotlSleepingWidth = 48;
        private final int axolotlSleepingHeight = 15;

        private final int axolotlHotbarUVx = 332;
        private final int axolotlHotbarUVy = 169;
        private final int axolotlHotbarWidth = 40;
        private final int axolotlHotbarHeight = 15;

        private final int axolotlBrownUVx = 373;
        private final int axolotlBrownUVy = 166;
        private final int axolotlBrownWidth = 35;
        private final int axolotlBrownHeight = 18;

        private final int axolotlPairUVx = 366;
        private final int axolotlPairUVy = 115;
        private final int axolotlPairWidth = 42;
        private final int axolotlPairHeight = 51;

        private final int axolotlYellowUVx = 408;
        private final int axolotlYellowUVy = 104;
        private final int axolotlYellowWidth = 25;
        private final int axolotlYellowHeight = 39;



        @Override
        public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
        {
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
