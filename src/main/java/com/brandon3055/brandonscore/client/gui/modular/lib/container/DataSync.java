/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.lib.container;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.container.data.AbstractDataStore;


import java.util.function.Supplier;

/**
 * This class provides a convenient way to synchronize server side data with a client side screen via the ContainerMenu
 * <p>
 * Created by brandon3055 on 09/09/2023
 */
public class DataSync<T> extends codechicken.lib.inventory.container.modular.DataSync<T> {
    public static final int PKT_SEND_CHANGES = 255;
    private final ModularGuiContainerMenu containerMenu;
    private final AbstractDataStore<T> dataStore;
    private final Supplier<T> valueGetter;

    public DataSync(ModularGuiContainerMenu containerMenu, AbstractDataStore<T> dataStore, Supplier<T> valueGetter) {
        this.containerMenu = containerMenu;
        this.dataStore = dataStore;
        this.valueGetter = valueGetter;
        containerMenu.dataSyncs.add(this);
    }

    public T get() {
        return dataStore.get();
    }

    /**
     * This should only ever be called server side!
     */
    public void detectAndSend() {
        if (dataStore.isSameValue(valueGetter.get())) {
            return;
        }
        dataStore.set(valueGetter.get());
        containerMenu.sendPacketToClient(PKT_SEND_CHANGES, buf -> {
            buf.writeByte((byte) containerMenu.dataSyncs.indexOf(this));
            dataStore.toBytes(buf);
        });
    }

    public void handleSyncPacket(net.minecraft.network.RegistryFriendlyByteBuf buf) {
        dataStore.fromBytes(buf);
    }
}
