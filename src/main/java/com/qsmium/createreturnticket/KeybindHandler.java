package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.gui.NotificationOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class KeybindHandler {

    // Register keybind during client setup
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeybindHandler::clientSetup);
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        // Register client tick event
        MinecraftForge.EVENT_BUS.register(KeybindHandler.class);
    }

    // Handle key press during client tick
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        // Check if the key is pressed
        if (Keybinds.notificationExpand.isDown())
        {
            //Inform NotificationOverlay
            NotificationOverlay.expandInfoKeyClicked = true;
        }
        else
        {
            NotificationOverlay.expandInfoKeyClicked = false;
        }
    }
}