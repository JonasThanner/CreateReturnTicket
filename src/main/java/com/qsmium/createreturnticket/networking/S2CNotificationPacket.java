package com.qsmium.createreturnticket.networking;

import com.qsmium.createreturnticket.NotificationManager;
import net.minecraft.network.FriendlyByteBuf;

public class S2CNotificationPacket
{

    public final NotificationManager.NotificationTypes notification;


    public S2CNotificationPacket(S2CNotificationPacket s2CNotificationPacket, FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(NotificationManager.NotificationTypes.class));
    }

    public S2CNotificationPacket(FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readEnum(NotificationManager.NotificationTypes.class));
    }

    //For Returns that use boolean as return type
    public S2CNotificationPacket(NotificationManager.NotificationTypes notification)
    {
        this.notification = notification;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf)
    {
        friendlyByteBuf.writeEnum(notification);
    }

}
