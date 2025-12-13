package com.qsmium.createreturnticket;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ReturnTicketAttacher
{
    //Create Register for Return Ticket Data Attachment - gets registered by ModMain
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModMain.MODID);

    //Register Return Ticket Data
    public static final Supplier<AttachmentType<ReturnTicketData>> RETURN_TICKET_ATTACHMENT = ATTACHMENT_TYPES.register(
            "return_ticket", () -> AttachmentType.serializable(ReturnTicketData::new).build()
    );
}
