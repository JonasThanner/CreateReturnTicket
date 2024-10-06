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
    private boolean rippedReturnTicket = true; //At the beginning no valid return ticket should exist
    private boolean invalidTicket = true; //Tickets are invalidated when a Player leaves the "Allowance" Zone between Train Transfers

    public void setEnterLocation(Vec3 newLocation)
    {
        enterLocation = newLocation;
    }
    public void setExitLocation(Vec3 newLocation)
    {
        exitLocation = newLocation;
    }
    public void ripReturnTicket() { rippedReturnTicket = true; }
    public void un_ripReturnTicket() { rippedReturnTicket = false; }
    public void invalidateTicket() { invalidTicket = true; }
    public void validateTicket() { invalidTicket = false; }


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


    @Override
    public CompoundTag serializeNBT() {
        //Create new NBT Tag
        final CompoundTag tag = new CompoundTag();

        //Save Enter Location to NBT
        tag.putDouble("enterX", enterLocation.x);
        tag.putDouble("enterY", enterLocation.y);
        tag.putDouble("enterZ", enterLocation.z);

        //Save Exit Location to NBT
        tag.putDouble("exitX", exitLocation.x);
        tag.putDouble("exitY", exitLocation.y);
        tag.putDouble("exitZ", exitLocation.z);

        //Save Ticket Validity
        tag.putBoolean("invalidTicket", invalidTicket);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

        //Retrieve Enter Location from NBT
        enterLocation = new Vec3(nbt.getDouble("enterX"), nbt.getDouble("enterY"), nbt.getDouble("enterZ"));

        //Retreive Exit Location from NBT
        exitLocation = new Vec3(nbt.getDouble("exitX"), nbt.getDouble("exitY"), nbt.getDouble("exitZ"));

        //Get Ticket validity
        invalidTicket = nbt.getBoolean("invalidTicket");
    }
}
