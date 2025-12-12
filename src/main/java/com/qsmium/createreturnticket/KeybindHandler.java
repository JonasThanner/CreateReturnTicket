package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.gui.NotificationOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = ModMain.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class KeybindHandler {

    // Register keybind during client setup
    public static void init() {
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(KeybindHandler::clientSetup);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // Register client tick event
        NeoForge.EVENT_BUS.register(KeybindHandler.class);
    }

    // Handle key press during client tick
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onClientTick(ClientTickEvent event)
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