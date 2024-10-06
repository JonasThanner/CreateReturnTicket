package com.qsmium.createreturnticket;



import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModMain.MODID)
public class ModMain
{

    public static final String MODID = "perfectplushiesfix";
    public static final Logger LOGGER = LogManager.getLogger();

    public ModMain()
    {

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        //    MinecraftForge.EVENT_BUS.register(new WhateverEvents());
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        //for client side only setup
    }

}


