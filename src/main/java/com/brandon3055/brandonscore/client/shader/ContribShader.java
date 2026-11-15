package com.brandon3055.brandonscore.client.shader;

import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class ContribShader extends BCShader<ContribShader> {
    public ContribShader(String path, VertexFormat format) { this(Identifier.fromNamespaceAndPath(BrandonsCore.MODID, path), format); }
    public ContribShader(Identifier location, VertexFormat format) { super(location, format); }
    public BCUniform getUv1OverrideUniform() { return uniform(80); }
    public BCUniform getUv2OverrideUniform() { return uniform(88); }
    public BCUniform getBaseColorUniform() { return uniform(64); }
    public BCUniform getTransitionUniform() { return uniform(104); }
    public BCUniform getHueUniform() { return uniform(108); }
    public boolean hasUv1OverrideUniform() { return true; }
    public boolean hasUv2OverrideUniform() { return true; }
    public boolean hasBaseColorUniform() { return true; }
    public boolean hasTransitionUniform() { return true; }
    public boolean hasHueUniform() { return true; }
}
