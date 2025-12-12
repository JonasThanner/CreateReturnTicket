package com.qsmium.createreturnticket;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class ReturnTicketData implements INBTSerializable<CompoundTag>
{
    private Vec3 enterLocation = new Vec3(0, 0, 0);
    private Vec3 exitLocation = new Vec3(0, 0, 0);
    private String enterDimension = "illegal_dim";
    private String exitDimension = "illegal_dim";
    private boolean rippedReturnTicket = true; //At the beginning no valid return ticket should exist
    private boolean invalidTicket = true; //Tickets are invalidated when a Player leaves the "Allowance" Zone between Train Transfers
    private int returnTicketAge = 0; //Counter that ticks up for every tick that the return ticket of a player is active. Needed for Ticket aging easteregg
    private String enterStation = "-";
    private String exitStation = "-";
    private boolean playerTransiting = false;

    public void setEnterLocation(Vec3 newLocation)
    {
        enterLocation = newLocation;
    }
    public void setExitLocation(Vec3 newLocation)
    {
        exitLocation = newLocation;
    }
    public void setEnterDimension(String newDim) { enterDimension = newDim; }
    public void setExitDimension(String newDim) { exitDimension = newDim; }
    public void setPlayerTransiting(boolean playerTransiting) { this.playerTransiting = playerTransiting; }
    public void ripReturnTicket() { rippedReturnTicket = true; }
    public void un_ripReturnTicket() { rippedReturnTicket = false; }
    public void invalidateTicket() { invalidTicket = true; }
    public void validateTicket() { invalidTicket = false; }
    public int getTicketAge() { return  returnTicketAge; }
    public void ageTicket(int addAge) { returnTicketAge += addAge; }
    public void setTicketAge(int newAge) { returnTicketAge = newAge; }
    public String getEnterStation() { return enterStation; }
    public String getExitStation() { return exitStation; }
    public String getEnterDimension() { return  enterDimension;}
    public String getExitDimension() { return exitDimension; }
    public boolean isPlayerTransiting() { return playerTransiting; }

    //Sets the enter Station
    //If the player enters the train after the enter train after it has left a station but is still
    //nearer to that station, a special character gets put infront of the text
    //If the player enters the train and the train is nearer to the target station, 'before' should be activated
    // => Same logic also applies to the exit station
    public void setEnterStation(String stationName, boolean before, boolean after) { enterStation = getStationIndicatorChar(before, after) + "§" + stationName; }
    public void setExitStation(String stationName, boolean before, boolean after) { exitStation = getStationIndicatorChar(before, after) + "§" + stationName; }



    public Vec3 getEnterLocation()
    {
        return enterLocation;
    }
    public Vec3 getExitLocation()
    {
        return exitLocation;
    }


    public boolean isReturnTicketRipped() {return rippedReturnTicket; }
    public boolean isValid() { return !invalidTicket; }

    public void resetStations()
    {
        enterStation = "-";
        exitStation = "-";
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        //Create new NBT Tag
        final CompoundTag tag = new CompoundTag();

        //Save Enter Location to NBT
        tag.putDouble("enterX", enterLocation.x);
        tag.putDouble("enterY", enterLocation.y);
        tag.putDouble("enterZ", enterLocation.z);

        //Save Enter Dimension
        tag.putString("enterDim", enterDimension);

        //Save Exit Location to NBT
        tag.putDouble("exitX", exitLocation.x);
        tag.putDouble("exitY", exitLocation.y);
        tag.putDouble("exitZ", exitLocation.z);

        //Save Exit Dimension
        tag.putString("exitDim", exitDimension);

        //Save actual Ticket Data
        tag.putBoolean("invalidTicket", invalidTicket);
        tag.putBoolean("ticketRipped", rippedReturnTicket);
        tag.putInt("ticketAge", returnTicketAge);
        tag.putString("enterStation", enterStation);
        tag.putString("exitStation", exitStation);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

        //Retrieve Enter Location from NBT
        enterLocation = new Vec3(compoundTag.getDouble("enterX"), compoundTag.getDouble("enterY"), compoundTag.getDouble("enterZ"));

        //Retrive Enter Dim from NBT
        enterDimension = compoundTag.getString("enterDim");

        //Retreive Exit Location from NBT
        exitLocation = new Vec3(compoundTag.getDouble("exitX"), compoundTag.getDouble("exitY"), compoundTag.getDouble("exitZ"));

        //Retrieve Exit Dim from NBT
        exitDimension = compoundTag.getString("exitDim");

        //Get Ticket Data
        invalidTicket = compoundTag.getBoolean("invalidTicket");
        rippedReturnTicket = compoundTag.getBoolean("ticketRipped");
        returnTicketAge = compoundTag.getInt("ticketAge");
        enterStation = compoundTag.getString("enterStation");
        exitStation = compoundTag.getString("exitStation");
    }

    private String getStationIndicatorChar(boolean before, boolean after)
    {
        return before ? "<Driving to>" : after ? "<Leaving from>" : "";
    }

}
