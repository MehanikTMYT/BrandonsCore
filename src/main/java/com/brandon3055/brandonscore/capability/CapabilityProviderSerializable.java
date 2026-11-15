package com.brandon3055.brandonscore.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.BaseCapability;
import com.brandon3055.brandonscore.capability.BCapabilityProvider;
import com.brandon3055.brandonscore.lib.datamanager.BCSerializable;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 19/11/2022
 */
public class CapabilityProviderSerializable<T extends BCSerializable<CompoundTag>> implements BCapabilityProvider, BCSerializable<CompoundTag> {

    protected final BaseCapability<T, ?> capability;
    protected final T instance;
    protected final Optional<T> instanceOpt;

    public CapabilityProviderSerializable(BaseCapability<T, ?> capability, T instance) {
        this.capability = capability;
        this.instance = instance;
        instanceOpt = Optional.of(this.instance);
    }

    @Nonnull
    @Override
    public <R> Optional<R> getCapability(@Nonnull BaseCapability<R, ?> cap, @Nullable Direction side) {
        if (capability == cap) {
            return instanceOpt.map(cap.typeClass()::cast);
        }
        return Optional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}
