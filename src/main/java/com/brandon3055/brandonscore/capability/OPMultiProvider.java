package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BaseCapability;
import com.brandon3055.brandonscore.capability.BCapabilityProvider;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 17/9/19.
 *
 * HANDLER = Handler / Storage / whatever you want to call your capability instance...
 */
public class OPMultiProvider implements BCapabilityProvider {

    public final Optional<IOPStorage> instance;
    public final Direction facing;

    public OPMultiProvider(Optional<IOPStorage> instance, @Nullable Direction facing) {
        this.instance = instance;
        this.facing = facing;
    }

    @Nonnull
    @Override
    public <T> Optional<T> getCapability(@Nonnull BaseCapability<T, ?> cap, @Nullable Direction side) {
        if (facing == null || facing == side) {
            if (cap == CapabilityOP.OP) {
                return instance.map(cap.typeClass()::cast);
            }
        }
        return Optional.empty();
    }
}
