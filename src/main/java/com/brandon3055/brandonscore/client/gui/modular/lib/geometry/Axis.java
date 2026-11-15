/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.lib.geometry;

/**
 * Created by brandon3055 on 02/09/2023
 */
public enum Axis {
    X,
    Y;

    public Axis opposite() {
        return this == X ? Y : X;
    }
}
