package com.qsmium.createreturnticket.networking;

import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.*;
import com.qsmium.createreturnticket.gui.ReturnTicketWidget;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import com.qsmium.createreturnticket.gui.TransitOverlay;
import com.qsmium.createreturnticket.rendering.RenderEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Supplier;

@EventBusSubscriber(modid = ModMain.MODID)
public class ReturnTicketPacketHandler
{

    public enum ClientToServerWork
    {
        REDEEM_TICKET,
        REQUEST_TICKET_STATUS,
        PRE_REDEEM_TICKET,
        RIP_TICKET,
        EMPTY_WORK
    }

    public enum ServerToClientWork
    {
        TICKET_EXISTENCE,
        TICKET_REDEEMABLE,
        TICKET_TOO_FAR,
        TICKET_AGED,
        TICKET_STATION_NAMES,
        TICKET_ENTER_POS,
        TICKET_EXIT_POS,
        TICKET_DIMENSIONS
    }

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        //Register Redeem Packet
        registrar.playToServer(
                C2SReturnTicketPacket.TYPE,
                C2SReturnTicketPacket.STREAM_CODEC,
                ReturnTicketPacketHandler::handleWorkFromClient
        );

        //Register To Client Packet for Work
        registrar.playToClient(
                S2CReturnTicketPacket.TYPE,
                S2CReturnTicketPacket.STREAM_CODEC,
                ReturnTicketPacketHandler::handleFromServer
        );

        //Register notification packet
        registrar.playToClient(
                S2CNotificationPacket.TYPE,
                S2CNotificationPacket.STREAM_CODEC,
                ReturnTicketPacketHandler::handleNotificationFromServer
        );
    }

    public static void sendRedeem()
    {
        PacketDistributor.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.REDEEM_TICKET));
    }

    public static void requestTicketStatus()
    {
        PacketDistributor.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.REQUEST_TICKET_STATUS));
    }

    public static  void preRedeemTicket()
    {
        PacketDistributor.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.PRE_REDEEM_TICKET));
    }

    public static void sendWorkToServer(ClientToServerWork newWork)
    {
        PacketDistributor.sendToServer(new C2SReturnTicketPacket(newWork));
    }

    public static void sendNotificationToPlayer(NotificationManager.NotificationTypes notificationType, ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new S2CNotificationPacket(notificationType));
    }

    public static void sendTooFarToPlayer(BlockPos blockPos, ServerPlayer player)
    {
        sendNotificationToPlayer(NotificationManager.NotificationTypes.EXIT_TOO_FAR, player);
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_TOO_FAR, blockPos));
    }

    public static void sendAgedTicketToPlayer(ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_AGED, true)); //AnswerResultBoolean is unnecessary here
    }

    public static void sendWorkToPlayer(ServerPlayer player, ServerToClientWork workType, boolean answer)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(workType, answer));
    }

    //Sends two packets to the client for the updated ticket station names
    //Uses the answer boolean to specify if its to or from station
    // true => to, false => from
    public static void sendTicketStationNames(ServerPlayer player, String enterStation, String exitStation)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, true, new BlockPos(0, 0, 0), enterStation));
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, false, new BlockPos(0, 0, 0), exitStation));
    }
    public static void sendTicketEnterStation(ServerPlayer player, String stationName)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, true, new BlockPos(0, 0, 0), stationName));
    }
    public static void sendTicketExitStation(ServerPlayer player, String stationName)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, false, new BlockPos(0, 0, 0), stationName));
    }

    public static void sendTicketEnterPosition(ServerPlayer player, Vec3 pos)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_ENTER_POS, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)));
    }

    public static void sendTicketExitPosition(ServerPlayer player, Vec3 pos)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_EXIT_POS, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)));
    }

    //Sends the Ticket Dimensions seperated by the § char
    public static void sendTicketDimension(ServerPlayer player, String enterDim, String exitDim)
    {
        PacketDistributor.sendToPlayer(player, new S2CReturnTicketPacket(ServerToClientWork.TICKET_DIMENSIONS, false, new BlockPos(0, 0, 0), enterDim + "§" + exitDim));
    }





    //Function to handle responses from the server
    //Possible responses can be
    // - Return Ticket Existence Answer
    //   => In that case its for setting the existence in the ReturnTicketWindow/Widget
    // - Return Ticket Redeemable
    //   => In this case its for asking if we can redeem a ticket that player is trying to redeem
    //   => If yes then we start the animation for redeeming the ticket
    //   => If no then we send a notification to the player that their ticket isnt redeemable
    //   TODO: We need to send a deny reason
    private static void handleFromServer(S2CReturnTicketPacket s2CReturnTicketPacket, final IPayloadContext context)
    {
        //Determine different cases
        switch (s2CReturnTicketPacket.serverToClientWork())
        {
            //First Case => We're asking for return ticket existence
            // => Set existence status in ClientDataHolder
            case TICKET_EXISTENCE:
                ClientTicketDataHolder.activeTicket = s2CReturnTicketPacket.answerTypeBoolean();
                if(ReturnTicketWidget.INSTANCE != null)
                {
                    ReturnTicketWidget.INSTANCE.setActive(ClientTicketDataHolder.activeTicket); //Race Condition Stuff i think -> Sometimes INSTANCE is null
                }
                break;

            //Second Case
            // => If it is redeemable then we start the animation for the transition screen
            case TICKET_REDEEMABLE:

                if(s2CReturnTicketPacket.answerTypeBoolean())
                {
                    TransitOverlay.startAnimation = true;
                    ReturnTicketWidget.deleteTicket();
                }
                break;

            //Third Case => The Server informs us that we're too far from the ticket exit location
            // => Get needed exit location and show it to the player
            case TICKET_TOO_FAR:

                RenderEventHandler.showBlock(s2CReturnTicketPacket.answerBlockPos());

                break;

            //Fourth case => Server informs us that our ticket is aged
            // => Inform rendering system
            case TICKET_AGED:
                ReturnTicketWidget.setTicketAged(s2CReturnTicketPacket.answerTypeBoolean());
                break;

            //Fifth case => Server informs us of new station names for our ticket
            // => Boolean True -> To Station
            //Then we need to seperate the indicator (if the train is leaving, going to the station) from the station name. They are seperated by a § symbol
            case TICKET_STATION_NAMES:

                if(s2CReturnTicketPacket.answerTypeBoolean())
                {
                    Pair<String, String> split = Util.SeperateString(s2CReturnTicketPacket.answerString(), "§");
                    ClientTicketDataHolder.enterStationDirectionIndicator = split.getFirst();
                    ClientTicketDataHolder.enterStation = split.getSecond();
                }
                else
                {
                    Pair<String, String> split = Util.SeperateString(s2CReturnTicketPacket.answerString(), "§");
                    ClientTicketDataHolder.exitStationDirectionIndicator = split.getFirst();
                    ClientTicketDataHolder.exitStation = split.getSecond();
                }

                break;

            //Server informs us of new station exit/enter dimensions
            //They are seperated by the § char
            case TICKET_DIMENSIONS:

                //Seperate the dimensions
                Pair<String, String> split = Util.SeperateString(s2CReturnTicketPacket.answerString(), "§");
                ClientTicketDataHolder.enterStationDimension = split.getFirst();
                ClientTicketDataHolder.exitStationDimension = split.getSecond();

                break;

            case TICKET_ENTER_POS:
                ClientTicketDataHolder.enterLocation = s2CReturnTicketPacket.answerBlockPos();
                ClientTicketDataHolder.enterLocExists = true;

                break;

            case TICKET_EXIT_POS:
                ClientTicketDataHolder.exitLocation = s2CReturnTicketPacket.answerBlockPos();
                ClientTicketDataHolder.exitLocExists = true;

                break;
        }
    }

    private static void handleNotificationFromServer(S2CNotificationPacket s2CNotificationPacket, final IPayloadContext context)
    {
        NotificationManager.newNotification(s2CNotificationPacket.notification());
    }

    private static void handleWorkFromClient(C2SReturnTicketPacket c2SRedeemReturnTicketPacket, final IPayloadContext context)
    {
        //Question to ask if the ticket even exists
        if(c2SRedeemReturnTicketPacket.work() == ClientToServerWork.REQUEST_TICKET_STATUS)
        {
            context.enqueueWork(() -> {
                // Get the player who sent the packet
                if (context.player() instanceof ServerPlayer player)
                {
                    //Determine the ticket status
                    boolean ticketExists = TicketManager.hasTicket(player);

                    // Send response packet back to the client
                    S2CReturnTicketPacket responsePacket = new S2CReturnTicketPacket(ServerToClientWork.TICKET_EXISTENCE, ticketExists);
                    PacketDistributor.sendToPlayer(player, responsePacket);
                }
            });

        }

        //PreRedeem gets called when the client first sends its request to the server when the players want to redeem a ticket
        //So when the player wants to redeem they first ask if the ticket is even valid
        // => If the ticket is valid => Play the transit animation and during that the ticket actually gets redemeed
        if(c2SRedeemReturnTicketPacket.work() == ClientToServerWork.PRE_REDEEM_TICKET)
        {
            context.enqueueWork(() -> {
                // Get the player who sent the packet
                if (context.player() instanceof ServerPlayer player) {
                    //Determine the ticket status
                    boolean ticketRedeemable = TicketManager.canRedeemTicket(player);

                    //Set the Player state to transitioning
                    // => Important for emergency teleport
                    TicketManager.setPlayerTransition(player, true);

                    // Send response packet back to the client
                    S2CReturnTicketPacket responsePacket = new S2CReturnTicketPacket(ServerToClientWork.TICKET_REDEEMABLE, ticketRedeemable);
                    PacketDistributor.sendToPlayer(player, responsePacket);
                }
            });
        }

        //Redeem the Ticket
        if(c2SRedeemReturnTicketPacket.work() == ClientToServerWork.REDEEM_TICKET)
        {
            context.enqueueWork(() -> {

                //If the Player exists, try to redeem their ticket
                if (context.player() instanceof ServerPlayer player)
                {
                    TicketManager.tryRedeemTicketServerside(player);
                }
            });
        }

        //Rip the Ticket
        if(c2SRedeemReturnTicketPacket.work() == ClientToServerWork.RIP_TICKET)
        {
            context.enqueueWork(() -> {

                //If the Player exists, rip their ticket
                if (context.player() instanceof ServerPlayer player)
                {
                    TicketManager.serversideRipTicket(player);
                }
            });
        }

    }




}
