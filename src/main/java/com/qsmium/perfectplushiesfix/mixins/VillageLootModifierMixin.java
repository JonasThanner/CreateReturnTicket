package com.qsmium.perfectplushiesfix.mixins;

import io.github.sirjain0.perfectplushies.loot.VillageLootModifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = VillageLootModifier.class, remap = false)
public abstract class VillageLootModifierMixin
{
    @Shadow @Final public List<TagEntry> plushies;
    @Shadow @Final private float chance;

    @Inject(method = "doApply", at = @At("HEAD"), remap = false, cancellable = true)
    public void init(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<@NotNull ObjectArrayList<ItemStack>> cir) {
        if (context.getRandom().nextFloat() < this.chance) {
            TagEntry tagthing = (TagEntry)this.plushies.get(context.getRandom().nextInt(this.plushies.size()));
            Item item;
            if (tagthing.isTag()) {
                TagKey<Item> itemTag = TagKey.create(Registries.ITEM, tagthing.getId());
                Optional<Item> optionalItem = ForgeRegistries.ITEMS.tags().getTag(itemTag).getRandomElement(context.getRandom());
                if (optionalItem.isPresent()) {
                    item = optionalItem.get();
                } else {
                    cir.setReturnValue(generatedLoot);

                    //Not really needed but ide doesnt like it because "item might not be initalized"
                    item = (Item)ForgeRegistries.ITEMS.getValue(tagthing.getId());
                }
            } else {
                item = (Item)ForgeRegistries.ITEMS.getValue(tagthing.getId());
            }

            generatedLoot.add(new ItemStack(item));
        }

        cir.setReturnValue(generatedLoot);
    }

}
