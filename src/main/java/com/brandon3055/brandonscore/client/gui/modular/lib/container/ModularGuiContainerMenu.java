/* Derived from CodeChickenLib; LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * See LICENSE-CCL and CCL-GUI-NOTICE.md. */
package com.brandon3055.brandonscore.client.gui.modular.lib.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

/** CCL network/quick-move integration with BC's complete historical slot groups. */
public abstract class ModularGuiContainerMenu extends codechicken.lib.inventory.container.modular.ModularGuiContainerMenu {
    protected ModularGuiContainerMenu(@Nullable MenuType<?> type, int id, Inventory inventory) {
        super(type, id, inventory);
    }

    @Override public Slot addSlot(Slot slot) { return super.addSlot(slot); }

    @Override protected SlotGroup createSlotGroup(int zone, int... quickMoveTo) {
        SlotGroup group = new SlotGroup(this, zone, quickMoveTo);
        slotGroups.add(group);
        return group;
    }
    @Override protected SlotGroup playerSlotGroup() { return createSlotGroup(0, 1); }
    @Override protected SlotGroup remoteSlotGroup() { return createSlotGroup(1, 0); }
}
