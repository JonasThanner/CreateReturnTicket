package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.ModMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record S2CReturnTicketPacket(ReturnTicketPacketHandler.ServerToClientWork serverToClientWork, boolean answerTypeBoolean, BlockPos answerBlockPos, String answerString) implements  CustomPacketPayload
{
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

    public static final CustomPacketPayload.Type<S2CReturnTicketPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "s2c_return_ticket_packet"));

    public static final StreamCodec<FriendlyByteBuf, S2CReturnTicketPacket> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ReturnTicketPacketHandler.ServerToClientWork.class),
            S2CReturnTicketPacket::serverToClientWork,
            ByteBufCodecs.BOOL,
            S2CReturnTicketPacket::answerTypeBoolean,
            BlockPos.STREAM_CODEC,
            S2CReturnTicketPacket::answerBlockPos,
            ByteBufCodecs.STRING_UTF8,
            S2CReturnTicketPacket::answerString,
            S2CReturnTicketPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
