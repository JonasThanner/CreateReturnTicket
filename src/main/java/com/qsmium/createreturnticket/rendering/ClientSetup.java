package com.qsmium.createreturnticket.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = ModMain.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        // Deferred rendering event registration
        NeoForge.EVENT_BUS.register(RenderEventHandler.class);
    }
}