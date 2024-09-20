package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.TicketManager;
import com.qsmium.createreturnticket.gui.ReturnTicketWidget;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ReturnTicketPacketHandler
{

    public enum WorkQuestion
    {
        REDEEM_TICKET,
        REQUEST_TICKET_STATUS,
        EMPTY_WORK
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
                .consumerMainThread(ReturnTicketPacketHandler::handleRedeem)
                .add();

        //Register Incoming Packet
        INSTANCE.messageBuilder(S2CReturnTicketPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CReturnTicketPacket::encode)
                .decoder(S2CReturnTicketPacket::new)
                .consumerMainThread(ReturnTicketPacketHandler::handleFromServer)
                .add();

    }

    public static void sendRedeem()
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(WorkQuestion.REDEEM_TICKET));
    }

    public static void requestTicketStatus()
    {
        INSTANCE.sendToServer(new C2SReturnTicketPacket(WorkQuestion.REQUEST_TICKET_STATUS));
    }
    private static void handleFromServer(S2CReturnTicketPacket s2CReturnTicketPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        //DEBUG
        //TODO: FUCK THIS
        ReturnTicketWindow.activeTicket = s2CReturnTicketPacket.returnTicketActive;

    }


    private static void handleRedeem(C2SReturnTicketPacket c2SRedeemReturnTicketPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {

        if(c2SRedeemReturnTicketPacket.workQuestion == WorkQuestion.REQUEST_TICKET_STATUS)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                // Get the player who sent the packet
                ServerPlayer player = contextSupplier.get().getSender();
                if (player != null) {
                    // Example logic to determine the ticket status
                    boolean ticketValid = TicketManager.hasTicket(player);

                    // Send response packet back to the client
                    S2CReturnTicketPacket responsePacket = new S2CReturnTicketPacket(ticketValid);
                    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), responsePacket);
                }
            });

        }

        if(c2SRedeemReturnTicketPacket.workQuestion == WorkQuestion.REDEEM_TICKET)
        {
            contextSupplier.get().enqueueWork(() -> {
                // Work that needs to be thread-safe (most work)
                ServerPlayer sender = contextSupplier.get().getSender(); // the client that sent this packet
                TicketManager.tryRedeemTicket(sender);
                // Do stuff
            });
        }

        contextSupplier.get().setPacketHandled(true);
    }


}
