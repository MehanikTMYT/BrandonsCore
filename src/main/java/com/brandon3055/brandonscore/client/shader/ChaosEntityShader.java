package com.brandon3055.brandonscore.client.shader;

import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class ChaosEntityShader extends BCShader<ChaosEntityShader> {
    public ChaosEntityShader(String path, VertexFormat format) { this(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, path), format); }
    public ChaosEntityShader(Identifier location, VertexFormat format) { super(location, format); uniform(128).glUniform1i(0); uniform(132).glUniform1i(0); }
    public BCUniform getYawUniform() { return uniform(112); }
    public BCUniform getPitchUniform() { return uniform(116); }
    public BCUniform getAlphaUniform() { return uniform(120); }
    public BCUniform getSimpleLightUniform() { return uniform(124); }
    public BCUniform getDisableLightUniform() { return uniform(128); }
    public BCUniform getDisableOverlayUniform() { return uniform(132); }
    public boolean hasYawUniform() { return true; }
    public boolean hasPitchUniform() { return true; }
    public boolean hasAlphaUniform() { return true; }
    public boolean hasSimpleLightUniform() { return true; }
    public boolean hasDisableLightUniform() { return true; }
    public boolean hasDisableOverlayUniform() { return true; }
}
