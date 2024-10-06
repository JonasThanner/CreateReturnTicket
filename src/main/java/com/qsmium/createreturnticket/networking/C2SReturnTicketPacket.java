package com.qsmium.createreturnticket.networking;

import net.minecraft.network.FriendlyByteBuf;

public class C2SReturnTicketPacket
{



    public final ReturnTicketPacketHandler.WorkQuestion workQuestion;

    public C2SReturnTicketPacket(C2SReturnTicketPacket c2SRedeemReturnTicketPacket, FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.WorkQuestion.class));
    }

    public C2SReturnTicketPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.WorkQuestion.class));
    }

    public C2SReturnTicketPacket(ReturnTicketPacketHandler.WorkQuestion workQuestion)
    {
        this.workQuestion = workQuestion;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeEnum(workQuestion);
    }
}
