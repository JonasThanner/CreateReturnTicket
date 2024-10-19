package com.qsmium.createreturnticket.networking;

import net.minecraft.network.FriendlyByteBuf;

public class C2SReturnTicketPacket
{



    public final ReturnTicketPacketHandler.ClientToServerWork clientToServerWork;

    public C2SReturnTicketPacket(C2SReturnTicketPacket c2SRedeemReturnTicketPacket, FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.ClientToServerWork.class));
    }

    public C2SReturnTicketPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.ClientToServerWork.class));
    }

    public C2SReturnTicketPacket(ReturnTicketPacketHandler.ClientToServerWork clientToServerWork)
    {
        this.clientToServerWork = clientToServerWork;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeEnum(clientToServerWork);
    }
}
