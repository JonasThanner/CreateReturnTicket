package com.qsmium.createreturnticket;

import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.GlobalStation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.openjdk.nashorn.internal.objects.Global;

import java.util.List;

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

    public static void serversideRipTicket(ServerPlayer player)
    {
        ReturnTicketData returnTicket = GetReturnTicket(player);

        if(returnTicket != null)
        {
            returnTicket.ripReturnTicket();
        }
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

    //TODO: Find more descriptive name
    //Function that updates the player on their station when they enter/exit a train
    //What its supposed  to do is we give it a player and the train they are on and if they are entering / exiting
    //If we are not currently standing at a station, we grab the nearest station to the train right now
    // => Either the one we are riding to or are riding from
    // => Then we determine which one is close and save that and if its the previous or next one
    //Once that is done, we send the information to the player
    public static void getPlayerClosestStation(ServerPlayer player, Train currentTrain, boolean mountingOn)
    {
        ReturnTicketData returnTicket = GetReturnTicket(player);

        if(returnTicket == null)
        {
            return;
        }

        //Variables for determining things later
        boolean toStationWinner = true;
        boolean stationary = false;
        String stationName = "-";

        //First we check if the train is at a current station, if it is, set station name and skip next checks
        GlobalStation stoppedStation = currentTrain.getCurrentStation();
        if(stoppedStation != null)
        {
            stationName = stoppedStation.name;
            stationary = true;
        }

        //First of all lets get the train schedule
        //If no schedule exists then all of the logic afterwards is moot and we just set the station as the Block Positions of the player
        Schedule schedule = currentTrain.runtime.getSchedule();
        if(schedule != null && stoppedStation == null)
        {
            //Grab the current and previous schedule entry
            // => What we are driving too and driving from
            Pair<DestinationInstruction, DestinationInstruction> scheduleEntries = GetScheduleEntries(currentTrain);

            //Check the destination instructions if there is a valid destination found
            if(scheduleEntries.getFirst() != null)
            {

                TrackGraph trackGraph = currentTrain.graph;

                //Now we have to actually get the GlobalStation of the first destination instruction
                //And if the second one exists, get that too to compare the two
                GlobalStation toStation = null;
                for(GlobalStation globalStation : trackGraph.getPoints(EdgePointType.STATION))
                {
                    if(!globalStation.name.matches(scheduleEntries.getFirst().getFilterForRegex()))
                        continue;

                    //Match found
                    toStation = globalStation;
                    break;
                }

                //Only continue if we have found a matching toStation
                if(toStation != null)
                {
                    //Search for fromStation, if it exists
                    GlobalStation fromStation = null;
                    if(scheduleEntries.getSecond() != null)
                    {
                        for(GlobalStation globalStation : trackGraph.getPoints(EdgePointType.STATION))
                        {
                            if(!globalStation.name.matches(scheduleEntries.getSecond().getFilterForRegex()))
                                continue;

                            //Match found
                            fromStation = globalStation;
                            break;
                        }
                    }

                    //Compare the two stations - if we actually can
                    //Based on comparison, pick the closest one

                    //If we can actually compare
                    if(fromStation != null)
                    {
                        BlockPos toPos = toStation.getBlockEntityPos();
                        Vec3 toPosVec = new Vec3(toPos.getX(), toPos.getY(), toPos.getZ());

                        BlockPos fromPos = fromStation.getBlockEntityPos();
                        Vec3 fromPosVec = new Vec3(fromPos.getX(), fromPos.getY(), fromPos.getZ());


                        //Get Player Pos
                        Vec3 playerPos = player.position();

                        //Compare
                        toStationWinner = playerPos.distanceTo(toPosVec) < playerPos.distanceTo(fromPosVec);
                    }

                    //Then once we have the closest one, we can set that station as the station name
                    DestinationInstruction winningStation = toStationWinner ? scheduleEntries.getFirst() : scheduleEntries.getSecond();
                    String regex = winningStation.getFilterForRegex();

                    //Clip the Regex
                    stationName = regex.substring(2, regex.length() - 2);

                }
            }
        }
        else
        {
            //Only do this if we arent stopped
            //Horrible Code btw
            if(stoppedStation == null)
            {
                BlockPos playerPos = player.blockPosition();
                stationName = "X: " + playerPos.getX() + "Z: " + playerPos.getZ() + "Y: " + playerPos.getY();
            }
        }

        //Now that we have the station name, we add the station to the player return ticketData and update the player on whats happening
        if(mountingOn)
        {
            returnTicket.setEnterStation(stationName, stationary ? false : toStationWinner, stationary ? false : !toStationWinner);
            ReturnTicketPacketHandler.sendTicketEnterStation(player, returnTicket.getEnterStation());
        }
        else
        {
            returnTicket.setExitStation(stationName, stationary ? false : toStationWinner, stationary ? false : !toStationWinner);
            ReturnTicketPacketHandler.sendTicketExitStation(player, returnTicket.getExitStation());
        }

    }

    //Function that searches for the next/current and the previous destination schedule entries in a schedule
    //The first Schedule Entry is the Entry the drain is driving *to* while the second is the entryy that the train is driving from
    //If there is no DestinationInstruction, it just returns null
    //If there is only a single destination instruction, the second, i.e the driving from destination is null
    private static Pair<DestinationInstruction, DestinationInstruction> GetScheduleEntries(Train train)
    {
        //First we try to iterate of the ScheduleInstructions of the train, starting from the current one and try to find the first DestinationEntry
        //If we have looped the entire schedule and found nothing => Null
        DestinationInstruction drivingToInstruction = FindFirstMatchingStation(train.runtime.getSchedule(), train.runtime.currentEntry, true);

        //If the first instruction is null we can just return with just nulls
        if(drivingToInstruction == null)
        {
            return new Pair<DestinationInstruction, DestinationInstruction>(null, null);
        }

        //Second we do the same thing but in reverse, i.e going backwards from the current instruction
        //If we find one, compare it to the first one. If they are the same, the second destination instruction will be null
        DestinationInstruction drivingFromInstruction = FindFirstMatchingStation(train.runtime.getSchedule(), train.runtime.currentEntry - 1, false);

        if(drivingFromInstruction == drivingToInstruction)
        {
            return new Pair<DestinationInstruction, DestinationInstruction>(drivingToInstruction, null);
        }

        return  new Pair<DestinationInstruction, DestinationInstruction>(drivingToInstruction, drivingFromInstruction);

    }

    private static DestinationInstruction FindFirstMatchingStation(Schedule schedule, int startIndex, boolean forward)
    {
        List<ScheduleEntry> entryList = schedule.entries;
        int size = entryList.size();

        // Iterate over the list in the specified direction
        for (int i = 0; i < size; i++) {
            // Calculate the current index based on direction and cyclic behavior
            int currentIndex = forward ? (startIndex + i) : (startIndex - i);

            if (schedule.cyclic) {
                currentIndex = (currentIndex + size) % size; // Wrap index cyclically
            } else if (currentIndex < 0 || currentIndex >= size) {
                break; // Stop if out of bounds and cyclic search is disabled
            }

            ScheduleEntry entry = entryList.get(currentIndex);

            // Check if the item is of the specified type
            if (entry.instruction instanceof DestinationInstruction destInstruct) {
                return destInstruct; // Return the first match
            }

            // Stop the search if we've completed a full cycle and cyclic search is enabled
            if (schedule.cyclic && i > 0 && currentIndex == startIndex) {
                break;
            }
        }

        // Return null if no matching instance is found
        return null;
    }

    public static ReturnTicketData GetReturnTicket(ServerPlayer player)
    {
        return player.getCapability(ReturnTicketAttacher.RETURN_TICKETS_MANAGER).orElse(null);
    }
}
