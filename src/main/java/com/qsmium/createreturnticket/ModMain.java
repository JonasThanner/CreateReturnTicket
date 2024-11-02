package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.gui.NotificationOverlay;
import com.qsmium.createreturnticket.gui.TransitOverlay;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
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

import java.util.List;
import java.util.ListIterator;

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


            ReturnTicketData returnTicket = eventPlayer.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

            eventPlayer.sendSystemMessage(Component.literal("Ticket age sent: " + returnTicket.getTicketAge())
                    .withStyle(ChatFormatting.GREEN), false);


            //Send ticket age if its aged
            if(TicketManager.isTicketAged(eventPlayer))
            {

                ReturnTicketPacketHandler.sendAgedTicketToPlayer(eventPlayer);
            }
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

                //If a Player enters a train the behavior has to be:
                // - Does the Player have an existing Return Ticket?
                //    => If no Give them one and validate it => Save Enter Location
                //    => If yes
                //       - Check if new Enter Location is Valid
                //           => If yes validate ticket just to make sure
                //           => If no invalidate ticket
                // - Check if the Player already has a valid Enter Location / ripped their Return Ticket
                // - If yes do nothing
                // - If not Save Enter Location & Set new Valid Enter Location / un-rip Return Ticket
                // -
                if (event.isMounting())
                {
                    Train currentTrain = Create.RAILWAYS.trains.get(carriage.trainId);

                    //Check if no Ticket exists => I.e is it ripped
                    // => I.e is this the first enterLocation?
                    if(returnTicket.isReturnTicketRipped())
                    {
                        //Give new ticket and save enter location
                        //And also validate the ticket at the beginning of the journey
                        returnTicket.un_ripReturnTicket();
                        returnTicket.setEnterLocation(player.getPosition(0));
                        returnTicket.validateTicket();

                        //Also save the station
                        TicketManager.getPlayerClosestStation(player, currentTrain, true);
                    }

                    //If a ticket does exist
                    else
                    {
                        //Check for Ticket validity
                        //Valid if Player is within Allowance Distance between previous exit location
                        if(player.getPosition(0).distanceTo(returnTicket.getExitLocation()) < 120)
                        {
                            //If valid then validate ticket just to make sure
                            returnTicket.validateTicket();
                        }

                        //If invalid
                        else
                        {
                            player.displayClientMessage(Component.literal("Your Return Ticket is invalid. This Journey will not Count. Consider ripping your Ticket"), true);
                            returnTicket.invalidateTicket();
                        }
                    }
                }

                //If a Player exits a train we have to
                // - Save the Exit location
                // - Save the Exit Station
                // - Notify Player of new Exit Location
                // - Reset Ticket Age
                if (event.isDismounting() && returnTicket.isValid())
                {
                    //Save new Exit Location
                    returnTicket.setExitLocation(player.getPosition(0));

                    //Save Exit Station
                    TicketManager.getPlayerClosestStation(player, Create.RAILWAYS.trains.get(carriage.trainId), false);

                    //Notify Player
                    ReturnTicketPacketHandler.sendNotificationToPlayer(NotificationManager.NotificationTypes.TICKET_UPDATED, player);
                    //player.displayClientMessage(Component.literal("Your Return Ticket was updated"), true);

                    //Reset Ticket Age internally and on client
                    returnTicket.setTicketAge(0);
                    ReturnTicketPacketHandler.sendWorkToPlayer(player, ReturnTicketPacketHandler.ServerToClientWork.TICKET_AGED, false);
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
                TicketManager.tryRedeemTicketServerside(player);
            }

            if(event.getItemStack().is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "stone"))) && event.getEntity() instanceof ServerPlayer)
            {
                NotificationManager.newNotification(NotificationManager.NotificationTypes.TICKET_UPDATED);
            }

            if(event.getItemStack().is(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "stick"))) && event.getEntity() instanceof ServerPlayer)
            {
                TransitOverlay.startAnimation = true;
            }


        }

        //In the Server Tick Event we need to
        // => Age Player Tickets
        @SubscribeEvent
        public static void onTickEvent(TickEvent.ServerTickEvent event)
        {
            //Get all players
            List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();

            //Go through players and age ticket data and check if they need to be informed of aged ticket
            for (ListIterator<ServerPlayer> iter = players.listIterator(); iter.hasNext(); )
            {
                //Get player
                ServerPlayer player = iter.next();

                //Age Ticket
                TicketManager.ageTicket(player, 1);


                if(TicketManager.isTicketAged(player))
                {
                    ReturnTicketPacketHandler.sendAgedTicketToPlayer(player);
                }

            }

        }




    }

}


