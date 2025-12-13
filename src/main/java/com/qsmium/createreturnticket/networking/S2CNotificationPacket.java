package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.NotificationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record S2CNotificationPacket(NotificationManager.NotificationTypes notification) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<S2CNotificationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModMain.MODID, "s2c_notification_packet"));

    public static final StreamCodec<FriendlyByteBuf, S2CNotificationPacket> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(NotificationManager.NotificationTypes.class),
            S2CNotificationPacket::notification,
            S2CNotificationPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
