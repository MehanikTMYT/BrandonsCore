package com.brandon3055.brandonscore.lib.datamanager;

import net.minecraft.nbt.Tag;

/** Compound-tag serialization contract retained by BrandonsCore on 26.1.2. */
public interface BCSerializable<T extends Tag> {
    T serializeNBT();
    void deserializeNBT(T tag);
}
