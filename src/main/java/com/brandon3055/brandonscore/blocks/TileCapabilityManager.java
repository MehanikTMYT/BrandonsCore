package com.brandon3055.brandonscore.blocks;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.BaseCapability;
import com.brandon3055.brandonscore.capability.BCapabilityProvider;
import com.brandon3055.brandonscore.lib.datamanager.BCSerializable;
import java.util.Optional;
import net.neoforged.neoforge.energy.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 18/12/19.
 */
public class TileCapabilityManager implements BCapabilityProvider {

    private Map<BaseCapability<?, ?>, Map<Direction, Optional<?>>> capabilityMap = new HashMap<>();
    private Map<Object, Predicate<Direction>> capSideValidator = new HashMap<>();

    private Map<BCSerializable<CompoundTag>, SerializationFlags<?>> serializableMap = new HashMap<>();
    private List<SerializationFlags<?>> indexedDataList = new ArrayList<>();
    private TileBCore tile;

    public TileCapabilityManager(TileBCore tile) {
        this.tile = tile;
    }

    /**
     * Bind the given capability instance to the specified sides while also invalidating and replacing
     * any existing capabilities of this type on the specified side.
     *
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param sides       The sides to bind to. (Leave empty to bind to all sides including null)
     */
    public <T> void set(@Nonnull BaseCapability<?, ?> cap, @Nonnull T capInstance, Direction... sides) {
        if (sides == null) {
            return;
        }
        if (sides.length == 0) {
            sides = Direction.values();
            setSide(cap, capInstance, null);
        }

        for (Direction dir : sides) {
            setSide(cap, capInstance, dir);
        }
    }

    /**
     * This method has the same functionality as {@link #set(Capability, Object, Direction...)}.
     * However it also adds capability to the management system that can automatically save, load and synchronize
     * the capability. This should only be used on static capability instances that dont need to be replaced runtime.
     *
     * @param tagName     The NBT name for this capability.
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param sides       The sides to bind to. (Leave empty to bind to all sides including null)
     * @return The modifiable serialization flags. By default set to 'save to tile' and 'save to item'
     * @see #set(Capability, Object, Direction...)
     */
    public <T extends BCSerializable<CompoundTag>> SerializationFlags<T> setManaged(String tagName, @Nonnull BaseCapability<?, ?> cap, @Nonnull T capInstance, Direction... sides) {
        set(cap, capInstance, sides);
        SerializationFlags<T> flags = new SerializationFlags<>(tagName, capInstance);
        serializableMap.put(capInstance, flags);
        indexedDataList.add(flags);
        return flags;
    }

    /**
     * The same as setManaged except the capability will not be exposes at all via getCapability. Used in cases where you need a "private" internal capability
     * @see #setManaged(String, Capability, BCSerializable, Direction...)
     */
    public <T extends BCSerializable<CompoundTag>> SerializationFlags<T> setInternalManaged(String tagName, @Nonnull BaseCapability<?, ?> cap, @Nonnull T capInstance) {
        SerializationFlags<T> flags = new SerializationFlags<>(tagName, capInstance);
        serializableMap.put(capInstance, flags);
        indexedDataList.add(flags);
        return flags;
    }

    /**
     * Invalidate and remove the specified capability type from the specified sides.
     *
     * @param cap   The capability type to remove.
     * @param sides The sides to remove from. (Leave empty to bind to all sides including null)
     */
    public <T> void remove(@Nonnull BaseCapability<T, ?> cap, Direction... sides) {
        if (sides.length == 0) {
            sides = Direction.values();
            clearSide(cap, null);
        }

        for (Direction dir : sides) {
            clearSide(cap, dir);
        }
    }

    /**
     * Bind the given capability instance to the specified side while also invalidating and replacing
     * any existing capability of this type.
     *
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param side        The side to bind to. (can be null)
     */
    public <T> void setSide(@Nonnull BaseCapability<?, ?> cap, @Nonnull T capInstance, @Nullable Direction side) {
        Map<Direction, Optional<?>> map = capabilityMap.computeIfAbsent(cap, c -> new HashMap<>());
        map.put(side, Optional.of(capInstance));

        tile.invalidateCapabilities();
    }

    /**
     * Invalidate and remove the specified capability type from the specified side.
     *
     * @param cap  The capability type to remove.
     * @param side The side to remove from. (can be null)
     */
    public <T> void clearSide(@Nonnull BaseCapability<?, ?> cap, @Nullable Direction side) {
        Map<Direction, Optional<?>> map = capabilityMap.get(cap);
        if (map != null) {
                map.remove(side);

            tile.invalidateCapabilities();
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getCapability(@Nonnull BaseCapability<T, ?> cap, @Nullable Direction side) {
        Map<Direction, Optional<?>> map = capabilityMap.get(cap);

        if (map != null && map.containsKey(side)) {
            Optional<T> optional = (Optional<T>) map.get(side);
            if (optional.filter(o -> capSideValidator.getOrDefault(o, d -> true).test(side)).isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    //Should be called by the tile's remove method to invalidate capabilities when the tile is removed.
    public void invalidate() {
        tile.invalidateCapabilities();
    }

    /**
     * This can give you more dynamic control over capability sidedness.
     * To use this you must first apply the capability to all sides via the standard add method.
     * Or at least all the sides you want to be able to give it access to.
     * Then this predicate can be used to dynamically block or allows access to the configured sides.
     *
     * @param capabilityInstance the capability instance (the same instance provided to the add method)
     * @param predicate          the predicate that determines whether or not the capability can be accessed from a given side.
     */
    public void setCapSideValidator(Object capabilityInstance, Predicate<Direction> predicate) {
        capSideValidator.put(capabilityInstance, predicate);
        tile.invalidateCapabilities();
    }

    //Serialization

    public CompoundTag serialize(boolean forItem) {
        CompoundTag compound = new CompoundTag();
        for (SerializationFlags<?> helper : serializableMap.values()) {
            if ((forItem && helper.saveItem) || (!forItem && helper.saveTile)) {
                compound.put(helper.tagName, helper.getData().serializeNBT());
            }
        }
        return compound;
    }

    @SuppressWarnings("unchecked")
    public void deserialize(CompoundTag compound) {
        for (SerializationFlags<?> helper : serializableMap.values()) {
            if (compound.contains(helper.tagName)) {
                helper.getData().deserializeNBT(compound.getCompoundOrEmpty(helper.tagName));
            }
        }
    }

    //Synchronization

    public void detectAndSendChanges() {
        for (int i = 0; i < indexedDataList.size(); i++) {
            SerializationFlags<?> helper = indexedDataList.get(i);
            if (helper.syncTile && helper.hasChanged(true)) {
                PacketCustom packet = createCapPacket(helper, i);
                packet.sendToChunk(tile);
            }
        }
    }

    public void detectAndSendChangesToListeners(Collection<Player> listeners) {
        for (int i = 0; i < indexedDataList.size(); i++) {
            SerializationFlags<?> helper = indexedDataList.get(i);
            if (helper.syncContainer && helper.hasChanged(true)) {
                PacketCustom packet = createCapPacket(helper, i);
                DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayer, p -> packet.sendToPlayer((ServerPlayer) p));
            }
        }
    }

    private PacketCustom createCapPacket(SerializationFlags<?> helper, int index) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_CAP_DATA, tile.getLevel().registryAccess());
        packet.writePos(tile.getBlockPos());
        packet.writeInt(index);
        if (helper.getData() instanceof IMCDataSerializable) {
            ((IMCDataSerializable) helper.getData()).serializeMCD(packet);
        } else {
            packet.writeCompoundNBT((CompoundTag) helper.getData().serializeNBT());
        }
        return packet;
    }

    @SuppressWarnings("unchecked")
    public void receiveCapSyncData(MCDataInput input) {
        int index = input.readInt();
        if (index >= 0 && index < indexedDataList.size()) {
            SerializationFlags<?> helper = indexedDataList.get(index);
            if (helper.getData() instanceof IMCDataSerializable) {
                ((IMCDataSerializable) helper.getData()).deSerializeMCD(input);
            } else {
                helper.getData().deserializeNBT(input.readCompoundNBT());
            }
        }
    }
}
