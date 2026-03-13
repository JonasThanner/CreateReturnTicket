package com.qsmium.createreturnticket;

import net.minecraft.core.BlockPos;

public class ClientTicketDataHolder
{
    //This Class holds all the relevant data about a ticket that the client needs to properly render a ticket
    //Display it, redeem it and all other things


    public static boolean activeTicket = false; //If the player even has a ticket
    public static BlockPos enterLocation;
    public static boolean enterLocExists = false;
    public static BlockPos exitLocation;
    public static boolean exitLocExists = false;
    public static String enterStation = "-";
    public static String exitStation = "-";
    public static String enterStationDirectionIndicator = "";
    public static String exitStationDirectionIndicator = "";
    public static String enterStationDimension = "";
    public static String exitStationDimension = "";
    public static float transferValidityDistanceSqr = 14400;
    public static boolean transferValid = false;
}
