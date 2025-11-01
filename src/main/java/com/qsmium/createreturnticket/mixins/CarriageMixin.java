package com.qsmium.createreturnticket.mixins;

import com.qsmium.createreturnticket.NotificationManager;
import com.qsmium.createreturnticket.TicketManager;
import com.qsmium.createreturnticket.networking.ReturnTicketPacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity.class, priority = 0)
public abstract class CarriageMixin
{
    //When the player gets dismounted from the DimensionalCarriage, we have to decide if this is due to a portal travel or not
    // => If we wouldnt catch this, players wouldnt be allowed to travel continuous journeys via the nether due to being dismounted & mounted
    // => If the player is captured, this means that we are travelling trough a portal. Otherwise i *THINK* we can ignore this
    //
    //When we do travel via Portal, we save a "grace" ticket for the player that can be consumed by the mount and dismount
    //This grace ticket is then consumed by the mount & dismount events
    @Inject(method = "dismountPlayer", at = @At("HEAD"), remap = false)
    public void dismountPlayer(ServerLevel sLevel, ServerPlayer sp, Integer seat, boolean capture, CallbackInfo ci)
    {
        //If we travel via portal
        if(capture)
        {
            //Give the server player a grace ticket
            TicketManager.dimensionTravelGraces.add(new TicketManager.DimensionTravelGrace(sp));
        }
    }
}
