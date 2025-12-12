package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.ModMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record C2SReturnTicketPacket(ReturnTicketPacketHandler.ClientToServerWork work) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<C2SReturnTicketPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "C2SReturnTicketPacket"));

    public static final StreamCodec<FriendlyByteBuf, C2SReturnTicketPacket> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ReturnTicketPacketHandler.ClientToServerWork.class),
            C2SReturnTicketPacket::work,
            C2SReturnTicketPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
