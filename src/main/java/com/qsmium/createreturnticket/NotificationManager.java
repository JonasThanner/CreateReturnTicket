package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.gui.NotificationOverlay;
import net.minecraft.network.chat.Component;

public class NotificationManager
{
    //Class that stores all the notifications and builds them when they are incoming

    public enum NotificationTypes
    {
        TICKET_UPDATED,
        TICKET_INVALIDATED,
        EXIT_TOO_FAR,
        WRONG_DIM
    }

    public static class CRTNotification
    {
        public String notifcationShort;
        public String notificationLong;
    }

    public static void newNotification(NotificationTypes newNotification)
    {
        CRTNotification newNotif = new CRTNotification();

        switch(newNotification)
        {
            case TICKET_UPDATED:
                newNotif.notifcationShort = "notification.createreturnticket.ticket_updated";
                newNotif.notificationLong = "longNotification.createreturnticket.ticket_updated";

                break;

            case EXIT_TOO_FAR:
                newNotif.notifcationShort = "notification.createreturnticket.ticket_too_far";
                newNotif.notificationLong = "longNotification.createreturnticket.ticket_too_far";

                break;

            case TICKET_INVALIDATED:
                newNotif.notifcationShort = "notification.createreturnticket.ticket_invalid_range_warning";
                newNotif.notificationLong = "longNotification.createreturnticket.ticket_invalid_range_warning";

                break;

            case WRONG_DIM:
                newNotif.notifcationShort = "notification.createreturnticket.wrong_dim";
                newNotif.notificationLong = "longNotification.createreturnticket.wrong_dim";

                break;
        }

        NotificationOverlay.addNotification(newNotif);

    }


}
