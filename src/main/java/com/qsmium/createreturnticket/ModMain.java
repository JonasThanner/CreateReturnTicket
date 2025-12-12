package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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

    }

    @EventBusSubscriber(modid = MODID)
    public static class CommonEvents
    {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event)
        {
            event.enqueueWork(() ->
            {
                ReturnTicketAttacher.ATTACHMENT_TYPES.register((IEventBus) event);
            });
        }

    }

    @EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
    public static class ServerEvents
    {
        @SubscribeEvent
        public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
        {
            ServerPlayer eventPlayer = (ServerPlayer) event.getEntity();
            ReturnTicketData returnTicket = eventPlayer.getData(ReturnTicketAttacher.RETURN_TICKET_ATTACHMENT);

            //Send Ticket Informations that need to be sent
            //Send ticket age if its aged
            if(TicketManager.isTicketAged(eventPlayer))
            {
                ReturnTicketPacketHandler.sendAgedTicketToPlayer(eventPlayer);
            }

            //Send Ticket Information
            if(returnTicket != null)
            {
                //Send Station Names
                ReturnTicketPacketHandler.sendTicketStationNames(eventPlayer, returnTicket.getEnterStation(), returnTicket.getExitStation());

                //Send Enter/Exit Locations
                //TODO: BOBOBOBO BAD BAD BAD
                //TODO: Saying exit pos doesnt exist if == 0 is bad but it works for now ig
                Vec3 pos = returnTicket.getEnterLocation();
                if(!pos.equals(new Vec3(0,0,0)))
                {
                    ReturnTicketPacketHandler.sendTicketEnterPosition(eventPlayer, pos);
                }
                pos = returnTicket.getExitLocation();
                if(!pos.equals(new Vec3(0,0,0)))
                {
                    ReturnTicketPacketHandler.sendTicketExitPosition(eventPlayer, pos);
                }

                //Send The Enter/Exit Dimensions
                ReturnTicketPacketHandler.sendTicketDimension(eventPlayer, returnTicket.getEnterDimension(), returnTicket.getExitDimension());
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
                ReturnTicketData returnTicket = player.getData(ReturnTicketAttacher.RETURN_TICKET_ATTACHMENT);

                //If a Player enters a train the behavior has to be:
                // - Does the Player have an existing Return Ticket?
                //    => If no Give them one and validate it => Save Enter Location
                //    => If yes
                //       - Check if new Enter Location is Valid
                //           => If yes validate ticket just to make sure
                //           => If no invalidate ticket
                // - Check if the Player already has a valid Enter Location / ripped their Return Ticket
                // - If yes do nothing
                // - If not Save Enter Location & Set new Valid Enter Location / un-rip Return Ticket & save enter dim
                // - Send the player the journey continuation message
                if (event.isMounting())
                {
                    Train currentTrain = Create.RAILWAYS.trains.get(carriage.trainId);

                    //Catch any issues where the train might be null => This CAN happen
                    if(currentTrain == null)
                    {
                        return;
                    }

                    //Check if no Ticket exists => I.e is it ripped
                    // => I.e is this the first enterLocation?
                    if(returnTicket.isReturnTicketRipped())
                    {
                        //Give new ticket and save enter location
                        //And also validate the ticket at the beginning of the journey
                        returnTicket.un_ripReturnTicket();
                        returnTicket.setEnterLocation(player.getPosition(0));
                        returnTicket.setEnterDimension(player.level().dimension().location().toString());
                        returnTicket.validateTicket();

                        //Also save the station and inform the player of it (function internally informs player)
                        TicketManager.getPlayerClosestStation(player, currentTrain, true);

                        //And inform player of the new dimensions
                        TicketManager.updatePlayerDimensions(player);
                    }

                    //If a ticket does exist
                    else
                    {
                        //Check for Ticket validity
                        //Valid if Player is within Allowance Distance between previous exit location or if they have a grace ticket
                        if(player.getPosition(0).distanceTo(returnTicket.getExitLocation()) < 120 || TicketManager.PlayerHasGrace(player, false))
                        {
                            //If valid then validate ticket just to make sure
                            returnTicket.validateTicket();
                        }

                        //If invalid
                        else
                        {
                            ReturnTicketPacketHandler.sendNotificationToPlayer(NotificationManager.NotificationTypes.TICKET_INVALIDATED, player);
                            returnTicket.invalidateTicket();

                            return;
                        }
                    }

                    //Send Player the new Continuation message
                    //Check for grace and consume it => This would be the natural endpoint for a dimensional travel
                    if(!TicketManager.PlayerHasGrace(player, true))
                    {
                        ReturnTicketPacketHandler.sendNotificationToPlayer(NotificationManager.NotificationTypes.JOURNEY_CONTINUING, player);
                    }
                }

                //If a Player exits a train we have to
                // - Save the Exit location
                // - Save the Exit Station
                // - Save the Exit Dim
                // - Notify Player of new Exit Location
                // - Reset Ticket Age
                if (event.isDismounting() && returnTicket.isValid() && !TicketManager.PlayerHasGrace(player, false))
                {
                    //Save new Exit Location
                    returnTicket.setExitLocation(player.getPosition(0));

                    //Save Exit Station
                    TicketManager.getPlayerClosestStation(player, Create.RAILWAYS.trains.get(carriage.trainId), false);

                    //Save the exit dim
                    returnTicket.setExitDimension(player.level().dimension().location().toString());

                    //Notify Player
                    ReturnTicketPacketHandler.sendNotificationToPlayer(NotificationManager.NotificationTypes.TICKET_UPDATED, player);
                    //player.displayClientMessage(Component.literal("Your Return Ticket was updated"), true);

                    //Reset Ticket Age internally and on client
                    returnTicket.setTicketAge(0);
                    ReturnTicketPacketHandler.sendWorkToPlayer(player, ReturnTicketPacketHandler.ServerToClientWork.TICKET_AGED, false);
                }
            }
        }

        //Players changing dimensions is a bit tricky, because it causes a dismount & mount if they are on the train
        // => This causes them to go outside of valid ticket range
        //So we need to somehow correlate this Event with the other Mount & Dismount Event so we can give the player immunity from the distance check and let them continue their journey
        // - Give the player immunity
        @SubscribeEvent
        public static void PlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event)
        {

        }

        //In the Server Tick Event we need to
        // => Age Player Tickets
        @SubscribeEvent
        public static void onTickEvent(ServerTickEvent event)
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

        //Emergency Teleport when a player whos transiting takes damage
        @SubscribeEvent
        public static void onPlayerDamaged(LivingDamageEvent event)
        {
            //Only execute if this is a server
            if (event.getEntity() instanceof ServerPlayer serverPlayer)
            {
                //If a player takes damage check if theyre transiting
                // => If so do the emergency redeem
                ReturnTicketData returnTicket = TicketManager.GetReturnTicket(serverPlayer);

                //If the player is transiting => Emergency Teleport
                if(returnTicket.isPlayerTransiting())
                {
                    TicketManager.tryRedeemTicketServerside(serverPlayer);
                }
            }
        }




    }

}


