package com.brandon3055.brandonscore.client;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

/** BC data extracted before entity layer submission. */
public final class BCClientRenderState {
    public static final ContextKey<Boolean> HIDE_VANILLA_WINGS =
            new ContextKey<>(Identifier.fromNamespaceAndPath("brandonscore", "hide_vanilla_wings"));
    private BCClientRenderState() {}
}
