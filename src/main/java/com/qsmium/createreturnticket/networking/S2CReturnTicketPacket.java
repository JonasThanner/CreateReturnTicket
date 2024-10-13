package com.qsmium.createreturnticket.networking;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class S2CReturnTicketPacket
{


    public final ReturnTicketPacketHandler.AnswerType answerType;
    public final boolean answerTypeBoolean;


    public S2CReturnTicketPacket(S2CReturnTicketPacket s2CReturnTicketPacket, FriendlyByteBuf friendlyByteBuf)
    {
        //All supported types get loaded in a row
        //The first type to get put into the bytebuff also needs to be the first to get loaded => FIFO
        // => So the answerType needs to be the last thing put into the bytebuff
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.AnswerType.class), friendlyByteBuf.readBoolean());
    }

    public S2CReturnTicketPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.AnswerType.class), friendlyByteBuf.readBoolean());
    }

    //For Returns that use boolean as return type
    public S2CReturnTicketPacket(ReturnTicketPacketHandler.AnswerType answerType, boolean answerResultBoolean)
    {
        this.answerType = answerType;
        this.answerTypeBoolean = answerResultBoolean;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeEnum(answerType);
        friendlyByteBuf.writeBoolean(answerTypeBoolean);
    }


}
