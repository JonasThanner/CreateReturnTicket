package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

    public static final String MODID = "createreturnticket";
    public static final Logger LOGGER = LogManager.getLogger();

    public ModMain()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        com.qsmium.createreturnticket.ModRegistry.BLOCKS.register(eventBus);
        com.qsmium.createreturnticket.ModRegistry.ITEMS.register(eventBus);
        com.qsmium.createreturnticket.ModRegistry.TILE_ENTITIES.register(eventBus);
        com.qsmium.createreturnticket.ConfigManager.setup();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        //    MinecraftForge.EVENT_BUS.register(new WhateverEvents());
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        //for client side only setup
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonEvents
    {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event)
        {
            event.enqueueWork(() ->
            {
                ReturnTicketPacketHandler.registerPackets();
            });
        }

    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ServerEvents
    {
        @SubscribeEvent
        public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
        {
            ServerPlayer eventPlayer = (ServerPlayer) event.getEntity();
            eventPlayer.sendSystemMessage(Component.literal("Your Penalty was reduced by 1!")
                    .withStyle(ChatFormatting.GREEN), false);
        }

        @SubscribeEvent
        public static void onMountEvent(EntityMountEvent event)
        {
            //Only check mounting if a player is mounting a train
            if ((event.getEntityBeingMounted() instanceof CarriageContraptionEntity carriage && event.getEntityMounting() instanceof Player) && !event.getLevel().isClientSide())
            {
                //Get Player mounting the train
                ServerPlayer player = (ServerPlayer) event.getEntityMounting();

                //Get return Ticket
                ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

                //If the returnTicket Capability doesnt exist we return (stupid but for now it works)
                if (returnTicket == null)
                {
                    return;
                }

                //If a Player enters a train the behavior has to be different
                //Ifa Player enters a train we have to
                // - Check if the Player already has a valid Enter Location / ripped their Return Ticket
                // - If yes do nothing
                // - If not Save Enter Location & Set new Valid Enter Location / un-rip Return Ticket
                // -
                if (event.isMounting() && returnTicket.isReturnTicketRipped())
                {
                    returnTicket.un_ripReturnTicket();
                    returnTicket.setEnterLocation(player.getPosition(0));
                }

                //If a Player exits a train we have to
                // - Save the Exit location
                // - Notify Player of new Exit Location
                if (event.isDismounting())
                {
                    //Save new Exit Location
                    returnTicket.setExitLocation(player.getPosition(0));

                    //Notify Player
                    player.displayClientMessage(Component.literal("Your Return Ticket was updated"), true);
                }
            }
        }

        //TODO: TESTING
        @SubscribeEvent
        public static void onUseEvent(PlayerInteractEvent.RightClickItem event)
        {
            if(event.getItemStack().is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "stick"))) && event.getEntity() instanceof ServerPlayer)
            {
                ServerPlayer player = (ServerPlayer) event.getEntity();
                TicketManager.tryRedeemTicket(player);
            }
        }


    }

}


