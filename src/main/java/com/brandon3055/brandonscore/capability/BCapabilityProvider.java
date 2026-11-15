package com.brandon3055.brandonscore.capability;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BaseCapability;
import javax.annotation.Nullable;

/** Internal capability collection; expose it through RegisterCapabilitiesEvent providers. */
public interface BCapabilityProvider {
    <T> Optional<T> getCapability(BaseCapability<T, ?> capability, @Nullable Direction side);
}
