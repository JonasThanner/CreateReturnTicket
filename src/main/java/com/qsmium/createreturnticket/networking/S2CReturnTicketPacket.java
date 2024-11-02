package com.qsmium.createreturnticket.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class S2CReturnTicketPacket
{


    public final ReturnTicketPacketHandler.ServerToClientWork serverToClientWork;
    public final boolean answerTypeBoolean;
    public final BlockPos answerBlockPos;
    public final String answerString;


    public S2CReturnTicketPacket(S2CReturnTicketPacket s2CReturnTicketPacket, FriendlyByteBuf friendlyByteBuf)
    {
        //All supported types get loaded in a row
        //The first type to get put into the bytebuff also needs to be the first to get loaded => FIFO
        // => So the answerType needs to be the last thing put into the bytebuff
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.ServerToClientWork.class), friendlyByteBuf.readBoolean());
    }

    public S2CReturnTicketPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(ReturnTicketPacketHandler.ServerToClientWork.class), friendlyByteBuf.readBoolean(), friendlyByteBuf.readBlockPos(), friendlyByteBuf.readUtf());
    }

    //For making a packet with just a string
    public S2CReturnTicketPacket(ReturnTicketPacketHandler.ServerToClientWork serverToClientWork, String answerString)
    {
        this(serverToClientWork, false, new BlockPos(0, 0, 0), answerString);
    }

    //For making a packet with just a block pos
    public S2CReturnTicketPacket(ReturnTicketPacketHandler.ServerToClientWork serverToClientWork, BlockPos blockPos)
    {
        this(serverToClientWork, false, blockPos, "");
    }

    //For making a packet with just a boolean
    public S2CReturnTicketPacket(ReturnTicketPacketHandler.ServerToClientWork serverToClientWork, boolean answerResultBoolean)
    {
        this(serverToClientWork, answerResultBoolean, new BlockPos(0, 0, 0), "");
    }

    //For Returns that use boolean as return type
    public S2CReturnTicketPacket(ReturnTicketPacketHandler.ServerToClientWork serverToClientWork, boolean answerResultBoolean, BlockPos blockpos, String answerString)
    {
        this.serverToClientWork = serverToClientWork;
        this.answerTypeBoolean = answerResultBoolean;
        this.answerBlockPos = blockpos;
        this.answerString = answerString;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeEnum(serverToClientWork);
        friendlyByteBuf.writeBoolean(answerTypeBoolean);
        friendlyByteBuf.writeBlockPos(answerBlockPos);
        friendlyByteBuf.writeUtf(answerString);
    }


}
