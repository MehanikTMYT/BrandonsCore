package com.brandon3055.brandonscore.worldentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/12/20
 */
public class WorldEntitySaveData extends SavedData {
    public static final String FILE_ID = "brandonscore_world_entity";
    public static final net.minecraft.world.level.saveddata.SavedDataType<WorldEntitySaveData> TYPE =
            new net.minecraft.world.level.saveddata.SavedDataType<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("brandonscore", FILE_ID),
                    WorldEntitySaveData::new,
                    CompoundTag.CODEC.xmap(WorldEntitySaveData::load, data -> {
                        if (data.saveCallback != null) data.saveCallback.run();
                        return data.save(new CompoundTag());
                    }));
    private List<WorldEntity> entities = new ArrayList<>();
    private Runnable saveCallback;

    public void updateEntities(List<WorldEntity> entities) {
        this.entities.clear();
        if (entities != null){
            this.entities.addAll(entities);
        }
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    public List<WorldEntity> getEntities() {
        return entities;
    }

    public static WorldEntitySaveData load(CompoundTag nbt) {
        WorldEntitySaveData data = new WorldEntitySaveData();
        ListTag list = nbt.getListOrEmpty("entities");
        for (Tag inbt : list) {
            if (inbt instanceof CompoundTag tag) {
                WorldEntity entity = WorldEntity.readWorldEntity(tag);
                if (entity != null) data.entities.add(entity);
            }
        }
        return data;
    }

    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (WorldEntity entity : entities) {
            CompoundTag entityTag = new CompoundTag();
            entity.write(entityTag);
            list.add(entityTag);
        }
        compound.put("entities", list);
        return compound;
    }

    @Override
    public boolean isDirty() {
        // World entities can mutate without notifying storage. Refresh at every world save.
        return true;
    }
}
