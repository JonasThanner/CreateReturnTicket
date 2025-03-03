package com.qsmium.createreturnticket.networking;

import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.*;
import com.qsmium.createreturnticket.gui.ReturnTicketWidget;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import com.qsmium.createreturnticket.gui.TransitOverlay;
import com.qsmium.createreturnticket.rendering.RenderEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

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
        TICKET_EXIT_POS
    }


    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModMain.MODID, "mainchannel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets()
    {
        //Register Redeem Packet
        INSTANCE.messageBuilder(C2SReturnTicketPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SReturnTicketPacket::encode)
                .decoder(C2SReturnTicketPacket::new)
                .consumerMainThread(ReturnTicketPacketHandler::handleFromClient)
                .add();

        //Register Incoming Packet
        INSTANCE.messageBuilder(S2CReturnTicketPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CReturnTicketPacket::encode)
                .decoder(S2CReturnTicketPacket::new)
                .consumerMainThread(ReturnTicketPacketHandler::handleFromServer)
                .add();

        //Register Outgoing 2 Client Notification Packet
        INSTANCE.messageBuilder(S2CNotificationPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CNotificationPacket::encode)
                .decoder(S2CNotificationPacket::new)
                .consumerMainThread(ReturnTicketPacketHandler::handleNotificationFromServer)
                .add();

    }

    public static void sendRedeem()
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.REDEEM_TICKET));
    }

    public static void requestTicketStatus()
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.REQUEST_TICKET_STATUS));
    }

    public static  void preRedeemTicket()
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(ClientToServerWork.PRE_REDEEM_TICKET));
    }

    public static void sendWorkToServer(ClientToServerWork newWork)
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(newWork));
    }

    public static void sendNotificationToPlayer(NotificationManager.NotificationTypes notificationType, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CNotificationPacket(notificationType));
    }

    public static void sendTooFarToPlayer(BlockPos blockPos, ServerPlayer player)
    {
        sendNotificationToPlayer(NotificationManager.NotificationTypes.EXIT_TOO_FAR, player);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_TOO_FAR, blockPos));
    }

    public static void sendAgedTicketToPlayer(ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_AGED, true)); //AnswerResultBoolean is unnecessary here
    }

    public static void sendWorkToPlayer(ServerPlayer player, ServerToClientWork workType, boolean answer)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(workType, answer));
    }

    //Sends two packets to the client for the updated ticket station names
    //Uses the answer boolean to specify if its to or from station
    // true => to, false => from
    public static void sendTicketStationNames(ServerPlayer player, String enterStation, String exitStation)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, true, new BlockPos(0, 0, 0), enterStation));
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, false, new BlockPos(0, 0, 0), exitStation));
    }
    public static void sendTicketEnterStation(ServerPlayer player, String stationName)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, true, new BlockPos(0, 0, 0), stationName));
    }
    public static void sendTicketExitStation(ServerPlayer player, String stationName)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_STATION_NAMES, false, new BlockPos(0, 0, 0), stationName));
    }

    public static void sendTicketEnterPosition(ServerPlayer player, Vec3 pos)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_ENTER_POS, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)));
    }

    public static void sendTicketExitPosition(ServerPlayer player, Vec3 pos)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CReturnTicketPacket(ServerToClientWork.TICKET_EXIT_POS, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)));
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
    private static void handleFromServer(S2CReturnTicketPacket s2CReturnTicketPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        //Determine different cases
        switch (s2CReturnTicketPacket.serverToClientWork)
        {
            //First Case => We're asking for return ticket existence
            // => Set existence status in the return ticket window
            case TICKET_EXISTENCE:
                ReturnTicketWindow.activeTicket = s2CReturnTicketPacket.answerTypeBoolean;
                ReturnTicketWidget.INSTANCE.setActive(s2CReturnTicketPacket.answerTypeBoolean);

                break;

            //Second Case
            // => If it is redeemable then we start the animation for the transition screen
            case TICKET_REDEEMABLE:

                if(s2CReturnTicketPacket.answerTypeBoolean)
                {
                    TransitOverlay.startAnimation = true;
                    ReturnTicketWidget.deleteTicket();
                }
                break;

            //Third Case => The Server informs us that we're too far from the ticket exit location
            // => Get needed exit location and show it to the player
            case TICKET_TOO_FAR:

                RenderEventHandler.showBlock(s2CReturnTicketPacket.answerBlockPos);

                break;

            //Fourth case => Server informs us that our ticket is aged
            // => Inform rendering system
            case TICKET_AGED:
                ReturnTicketWidget.setTicketAged(s2CReturnTicketPacket.answerTypeBoolean);
                break;

            //Fifth case => Server informs us of new station names for our ticket
            // => Boolean True -> To Station
            //Then we need to seperate the indicator (if the train is leaving, going to the station) from the station name. They are seperated by a § symbol
            case TICKET_STATION_NAMES:

                if(s2CReturnTicketPacket.answerTypeBoolean)
                {
                    Pair<String, String> split = Util.SeperateString(s2CReturnTicketPacket.answerString, "§");
                    ClientTicketDataHolder.enterStationDirectionIndicator = split.getFirst();
                    ClientTicketDataHolder.enterStation = split.getSecond();
                }
                else
                {
                    Pair<String, String> split = Util.SeperateString(s2CReturnTicketPacket.answerString, "§");
                    ClientTicketDataHolder.exitStationDirectionIndicator = split.getFirst();
                    ClientTicketDataHolder.exitStation = split.getSecond();
                }

                break;

            case TICKET_ENTER_POS:
                ClientTicketDataHolder.enterLocation = s2CReturnTicketPacket.answerBlockPos;
                ClientTicketDataHolder.enterLocExists = true;

                break;

            case TICKET_EXIT_POS:
                ClientTicketDataHolder.exitLocation = s2CReturnTicketPacket.answerBlockPos;
                ClientTicketDataHolder.exitLocExists = true;

                break;
        }

        contextSupplier.get().setPacketHandled(true);
    }

    private static void handleNotificationFromServer(S2CNotificationPacket s2CNotificationPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NotificationManager.newNotification(s2CNotificationPacket.notification);
        contextSupplier.get().setPacketHandled(true);
    }

    private static void handleFromClient(C2SReturnTicketPacket c2SRedeemReturnTicketPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {

        //Question to ask if the ticket even exists
        if(c2SRedeemReturnTicketPacket.clientToServerWork == ClientToServerWork.REQUEST_TICKET_STATUS)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                // Get the player who sent the packet
                ServerPlayer player = contextSupplier.get().getSender();
                if (player != null) {
                    // Example logic to determine the ticket status
                    boolean ticketExists = TicketManager.hasTicket(player);

                    // Send response packet back to the client
                    S2CReturnTicketPacket responsePacket = new S2CReturnTicketPacket(ServerToClientWork.TICKET_EXISTENCE, ticketExists);
                    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
                }
            });

        }

        //PreRedeem gets called when the client first sends its request to the server when the players want to redeem a ticket
        //So when the player wants to redeem they first ask if the ticket is even valid
        // => If the ticket is valid => Play the transit animation and during that the ticket actually gets redemeed
        if(c2SRedeemReturnTicketPacket.clientToServerWork == ClientToServerWork.PRE_REDEEM_TICKET)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Get the player who sent the packet
                ServerPlayer player = contextSupplier.get().getSender();
                if (player != null) {
                    // Example logic to determine the ticket status
                    boolean ticketRedeemable = TicketManager.canRedeemTicket(player);

                    // Send response packet back to the client
                    S2CReturnTicketPacket responsePacket = new S2CReturnTicketPacket(ServerToClientWork.TICKET_REDEEMABLE, ticketRedeemable);
                    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
                }
            });
        }

        if(c2SRedeemReturnTicketPacket.clientToServerWork == ClientToServerWork.REDEEM_TICKET)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                ServerPlayer sender = contextSupplier.get().getSender(); // the client that sent this packet
                TicketManager.tryRedeemTicketServerside(sender);
                // Do stuff
            });
        }

        if(c2SRedeemReturnTicketPacket.clientToServerWork == ClientToServerWork.RIP_TICKET)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                ServerPlayer sender = contextSupplier.get().getSender(); // the client that sent this packet
                TicketManager.serversideRipTicket(sender);
                // Do stuff
            });
        }

        contextSupplier.get().setPacketHandled(true);
    }




}
