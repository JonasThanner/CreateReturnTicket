package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.TicketManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ReturnTicketPacketHandler
{
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
        INSTANCE.messageBuilder(C2SRedeemReturnTicketPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRedeemReturnTicketPacket::new)
                .decoder(C2SRedeemReturnTicketPacket::new)
                .consumerMainThread(ReturnTicketPacketHandler::handleRedeem)
                .add();

    }

    public static void sendRedeem()
    {
        INSTANCE.sendToServer(new C2SRedeemReturnTicketPacket());
    }

    private static void handleRedeem(C2SRedeemReturnTicketPacket c2SRedeemReturnTicketPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> {
            // Work that needs to be thread-safe (most work)
            ServerPlayer sender = contextSupplier.get().getSender(); // the client that sent this packet
            TicketManager.tryRedeemTicket(sender);
            // Do stuff
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
