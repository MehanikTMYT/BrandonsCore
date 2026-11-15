package com.brandon3055.brandonscore.items;

import com.brandon3055.brandonscore.capability.MultiCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;

import javax.annotation.Nullable;

@Deprecated //incomplete
public interface EnergyItemImpl extends IItemExtension {

    // Internal collection factory; item capabilities must be registered with RegisterCapabilitiesEvent.
    default MultiCapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        MultiCapabilityProvider provider = new MultiCapabilityProvider();
        //
        addAdditionalCapabilities(stack, provider);
        return provider;
    }

    default void addAdditionalCapabilities(ItemStack stack, MultiCapabilityProvider provider) {}
}
