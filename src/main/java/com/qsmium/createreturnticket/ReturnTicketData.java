package com.qsmium.createreturnticket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
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
    public CompoundTag serializeNBT() {
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
    public void deserializeNBT(CompoundTag nbt) {

        //Retrieve Enter Location from NBT
        enterLocation = new Vec3(nbt.getDouble("enterX"), nbt.getDouble("enterY"), nbt.getDouble("enterZ"));

        //Retrive Enter Dim from NBT
        enterDimension = nbt.getString("enterDim");

        //Retreive Exit Location from NBT
        exitLocation = new Vec3(nbt.getDouble("exitX"), nbt.getDouble("exitY"), nbt.getDouble("exitZ"));

        //Retrieve Exit Dim from NBT
        exitDimension = nbt.getString("exitDim");

        //Get Ticket Data
        invalidTicket = nbt.getBoolean("invalidTicket");
        rippedReturnTicket = nbt.getBoolean("ticketRipped");
        returnTicketAge = nbt.getInt("ticketAge");
        enterStation = nbt.getString("enterStation");
        exitStation = nbt.getString("exitStation");
    }

    private String getStationIndicatorChar(boolean before, boolean after)
    {
        return before ? "<Driving to>" : after ? "<Leaving from>" : "";
    }
}
