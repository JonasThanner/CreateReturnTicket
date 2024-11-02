package com.qsmium.createreturnticket;

import net.minecraft.world.phys.Vec3;

public class ClientTicketDataHolder
{
    //This Class holds all the relevant data about a ticket that the client needs to properly render a ticket
    //Display it, redeem it and all other things


    public static boolean activeTicket = false; //If the player even has a ticket
    public static Vec3 exitPos = new Vec3(300000000, 0, 0);
    public static String enterLocation = "";
    public static String exitLocation = "";
    public static String enterStation = "-";
    public static String exitStation = "-";
    public static String enterStationDirectionIndicator = "";
    public static String exitStationDirectionIndicator = "";
}
