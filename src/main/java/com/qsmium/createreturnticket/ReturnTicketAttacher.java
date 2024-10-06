package com.qsmium.createreturnticket;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod(ModMain.MODID)
@Mod.EventBusSubscriber(modid = ModMain.MODID)
public class ReturnTicketAttacher
{

    public static Capability<ReturnTicketData> RETURN_TICKETS_MANAGER = CapabilityManager.get(new CapabilityToken<ReturnTicketData>() {});

    private static class ReturnTicketProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
    {

        public static final ResourceLocation RETURN_TICKET = new ResourceLocation(ModMain.MODID, "returnticket");

        private final ReturnTicketData backend = new ReturnTicketData();
        private final LazyOptional<ReturnTicketData> optionalData = LazyOptional.of(() -> backend);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return RETURN_TICKETS_MANAGER.orEmpty(cap, this.optionalData);
        }

        void invalidate() {
            this.optionalData.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event)
    {

        if (event.getObject() instanceof Player)
        {
            final ReturnTicketProvider provider = new ReturnTicketProvider();
            event.addCapability(ReturnTicketProvider.RETURN_TICKET, provider);
        }
    }
}
