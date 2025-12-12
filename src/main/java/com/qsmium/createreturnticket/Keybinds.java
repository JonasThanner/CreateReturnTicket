package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.gui.NotificationOverlay;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ModMain.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class Keybinds {

    public static final String CATEGORY = "Create: Return Ticket";
    public static KeyMapping notificationExpand;

    // Method to register key bindings
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        notificationExpand = new KeyMapping("key" + ModMain.MODID + "example", GLFW.GLFW_KEY_LEFT_ALT, CATEGORY); // Example keybind
        event.register(notificationExpand);
    }
}