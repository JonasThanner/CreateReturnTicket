package com.qsmium.createreturnticket.networking;

import net.minecraft.network.FriendlyByteBuf;

public class S2CReturnTicketPacket
{


//    public ReturnTicketPacketHandler.WorkQuestion workQuestion = ReturnTicketPacketHandler.WorkQuestion.REDEEM_TICKET;
    public final boolean returnTicketActive;

    public S2CReturnTicketPacket(S2CReturnTicketPacket s2CReturnTicketPacket, FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readBoolean());
    }

    public S2CReturnTicketPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readBoolean());
    }

    public S2CReturnTicketPacket(boolean returnTicketActive)
    {
        this.returnTicketActive = returnTicketActive;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeBoolean(returnTicketActive);
    }


}
