package com.qsmium.createreturnticket;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ModMain.MODID)
@Mod.EventBusSubscriber(modid = ModMain.MODID, value = Dist.DEDICATED_SERVER)
public class RegisterCap
{
    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event)
    {
        //event.register(ReturnTicketData.class);
    }
}
