package com.brandon3055.brandonscore.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.BaseCapability;
import com.brandon3055.brandonscore.capability.BCapabilityProvider;
import com.brandon3055.brandonscore.lib.datamanager.BCSerializable;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by brandon3055 on 17/4/20.
 */
public class MultiCapabilityProvider implements BCapabilityProvider, BCSerializable<CompoundTag> {

    private Map<BaseCapability<?, ?>, Object> capabilityMap = new HashMap<>();
    private Map<String, BCSerializable<CompoundTag>> nameMap = new HashMap<>();

    public MultiCapabilityProvider() {}

    public MultiCapabilityProvider(BCSerializable<CompoundTag> capInstance, String name, BaseCapability<?, ?>... capabilities) {
        addCapability(capInstance, name, capabilities);
    }

    public void addCapability(BCSerializable<CompoundTag> capInstance, String name, BaseCapability<?, ?>... capabilities) {
        if (CapabilityOP.OP == null) return;
        this.nameMap.put(name, capInstance);
        for (BaseCapability<?, ?> cap : capabilities) {
            Objects.requireNonNull(cap);
            this.capabilityMap.put(cap, capInstance);
        }
    }

    public <T> void addUnsavedCap(BaseCapability<T, ?> capability, T capInstance) {
        if (CapabilityOP.OP == null) return;
        this.capabilityMap.put(capability, capInstance);
    }

    @Nonnull
    @Override
    public <T> Optional<T> getCapability(@Nonnull BaseCapability<T, ?> cap, @Nullable Direction side) {
        if (capabilityMap.containsKey(cap)) {
            return Optional.of(cap.typeClass().cast(capabilityMap.get(cap)));
        }
        return Optional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        nameMap.forEach((s, t) -> tag.put(s, t.serializeNBT()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        nameMap.forEach((s, t) -> t.deserializeNBT(nbt.getCompoundOrEmpty(s)));
    }
}
