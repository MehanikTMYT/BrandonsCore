package com.brandon3055.brandonscore.lib.datamanager;
import net.minecraft.nbt.CompoundTag;

// port-26.1.2: аналог удалённого BCSerializable (сериализация через CompoundTag)
public interface BCSerializable { 
    CompoundTag serializeNBT(); 
    void deserializeNBT(CompoundTag tag); 
}
