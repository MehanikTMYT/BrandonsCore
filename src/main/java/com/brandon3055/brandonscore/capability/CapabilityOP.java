package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.blocks.TileBCore;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/** Long-valued Operational Potential capabilities for 26.1.2. */
public final class CapabilityOP {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "op");
    public static final BlockCapability<IOPStorage, Direction> OP = BlockCapability.createSided(ID, IOPStorage.class);
    public static final ItemCapability<IOPStorage, ItemAccess> ITEM = ItemCapability.create(ID, IOPStorage.class, ItemAccess.class);

    private CapabilityOP() {}

    public static void registerItem(RegisterCapabilitiesEvent event, net.minecraft.world.item.Item item,
            java.util.function.BiFunction<net.minecraft.world.item.ItemStack, ItemAccess, IOPStorage> provider) {
        event.registerItem(ITEM, provider::apply, item);
    }

    /** Call from the consuming mod's RegisterCapabilitiesEvent listener for each TileBCore type. */
    public static <T extends TileBCore> void registerBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(OP, type, (tile, side) -> tile.getCapability(OP, side).orElse(null));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, type,
                (tile, side) -> tile.getCapability(OP, side)
                        .filter(net.neoforged.neoforge.transfer.energy.EnergyHandler.class::isInstance)
                        .map(net.neoforged.neoforge.transfer.energy.EnergyHandler.class::cast).orElse(null));
    }
}
