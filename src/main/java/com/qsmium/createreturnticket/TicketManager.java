package com.qsmium.createreturnticket;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TicketManager
{
    //To redeem a Ticket we have to
    // - Check if Player Ticket is Valid/Not Ripped
    // - Check if Player is withing valid Block distance of Exit Point
    public static boolean tryRedeemTicket(ServerPlayer player)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        //Return if no Ticket Capability found
        if(returnTicket == null)
            return false;

        //Check Validity of Ticket
        //If ticket isn't valid we inform the Player of his ticket not being Valid and return
        if(returnTicket.isReturnTicketRipped())
        {
            player.displayClientMessage(Component.literal("You have no active Return Ticket").withStyle(ChatFormatting.DARK_RED), false);
            return false;
        }

        //Check distance of Player to Exit Point
        //If the player is further away we inform the player and return
        //TODO: Hardcoded distance
        if(player.getPosition(0).distanceTo(returnTicket.getExitLocation()) > 10)
        {
            player.displayClientMessage(Component.literal("Youre too Far from the Exit Location: " + returnTicket.getExitLocation()).withStyle(ChatFormatting.DARK_RED), false);
            return false;
        }

        //If everything else is met redeem the Ticket
        //To Redeem Ticket we
        // - Rip the Ticket / Invalidate it
        // - Teleport the Player to EnterLocation
        returnTicket.ripReturnTicket();
        player.teleportTo(returnTicket.getEnterLocation().x, returnTicket.getEnterLocation().y, returnTicket.getEnterLocation().z);
        return true;

    }
}
