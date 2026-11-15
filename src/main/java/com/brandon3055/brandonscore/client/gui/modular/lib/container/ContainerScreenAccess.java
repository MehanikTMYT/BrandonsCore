/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.lib.container;

import com.brandon3055.brandonscore.client.gui.modular.elements.GuiSlots;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Used by {@link ContainerGuiProvider}
 * Provides access to the ContainerMenu as well as some essential functions.
 * <p>
 * Created by brandon3055 on 08/09/2023
 */
public interface ContainerScreenAccess<T extends AbstractContainerMenu> extends MenuAccess<T> {

    /**
     * This is the modular gui friendly method used by elements such as {@link GuiSlots} to render inventory item stacks.
     */
    void renderSlot(GuiRender render, Slot slot);

}
