package com.qsmium.createreturnticket;

import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TicketManager
{
    //To redeem a Ticket we have to
    // - Check if Player Ticket is Valid/Not Ripped
    // - Check if Player is withing valid Block distance of Exit Point
    public static boolean tryRedeemTicketServerside(ServerPlayer player)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        //Return if no Ticket Capability found
        if(returnTicket == null)
            return false;

        //Check Existence of Ticket
        //If ticket isn't valid we inform the Player of his ticket not being Valid and return
        if(returnTicket.isReturnTicketRipped())
        {
            player.displayClientMessage(Component.literal("You have no active Return Ticket").withStyle(ChatFormatting.DARK_RED), false);
            return false;
        }

        //Check Ticket Validity
        if(!returnTicket.isValid())
        {
            player.displayClientMessage(Component.literal("Youre Ticket isnt Valid! Your journey needs to be contigous").withStyle(ChatFormatting.DARK_RED), false);
            return false;
        }

        //Check distance of Player to Exit Point
        //If the player is further away we inform the player and return
        //TODO: Hardcoded distance
        if(player.getPosition(0).distanceTo(returnTicket.getExitLocation()) > 10)
        {
            Vec3 blockpos = returnTicket.getExitLocation();
            ReturnTicketPacketHandler.sendTooFarToPlayer(new BlockPos((int) blockpos.x, (int) blockpos.y, (int) blockpos.z), player);

            //player.displayClientMessage(Component.literal("Youre too Far from the Exit Location: " + returnTicket.getExitLocation()).withStyle(ChatFormatting.DARK_RED), false);
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

    //Function thats called when we try a clientside ticket redemption
    //To do so we have to
    // - Send a ticket valid question to the server
    public static void tryRedeemTicketClient()
    {

    }

    //Function that checks if a player can even redeem a ticket that they have
    public static boolean canRedeemTicket(ServerPlayer player)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        //Check Ticket Validity
        if(!returnTicket.isValid())
        {
            ReturnTicketPacketHandler.sendNotificationToPlayer(NotificationManager.NotificationTypes.TICKET_INVALIDATED, player);
            return false;
        }

        if(player.getPosition(0).distanceTo(returnTicket.getExitLocation()) > 10)
        {
            Vec3 blockpos = returnTicket.getExitLocation();
            ReturnTicketPacketHandler.sendTooFarToPlayer(new BlockPos((int) blockpos.x, (int) blockpos.y, (int) blockpos.z), player);

            //player.displayClientMessage(Component.literal("Youre too Far from the Exit Location: " + returnTicket.getExitLocation()).withStyle(ChatFormatting.DARK_RED), false);
            return false;
        }

        return true;
    }

    //Function to call if you want to know if the player has an active ticket
    public static boolean hasTicket(ServerPlayer player)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        //Return if no Ticket Capability found
        if(returnTicket == null)
            return false;

        return !returnTicket.isReturnTicketRipped();
    }

    public static boolean isTicketAged(ServerPlayer player)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        if(returnTicket == null)
            return false;

        return returnTicket.getTicketAge() > 2000;
    }

    public static void ageTicket(ServerPlayer player, int amount)
    {
        //Get ReturnTicket
        ReturnTicketData returnTicket = player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);

        if(returnTicket == null)
            return;

        if(returnTicket.isReturnTicketRipped())
            return;

        returnTicket.ageTicket(amount);

        //Debug
        player.displayClientMessage(Component.literal(Integer.toString(returnTicket.getTicketAge())), true);
    }
}
