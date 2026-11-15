package com.brandon3055.brandonscore.client.shader;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import java.nio.*;
import java.util.*;
import java.util.function.Consumer;

/** MC26 pipeline and extraction-time parameters, with no GL state or shader-instance globals. */
public class BCShader<T extends BCShader<T>> {
    static final int BLOCK_SIZE = 176;
    final ByteBuffer parameters = ByteBuffer.allocate(BLOCK_SIZE).order(ByteOrder.nativeOrder());
    final Identifier location;
    final VertexFormat format;
    private final Map<VertexFormat.Mode, RenderPipeline> pipelines = new EnumMap<>(VertexFormat.Mode.class);
    private final List<Consumer<T>> callbacks = new ArrayList<>();
    public BCShader(Identifier location, VertexFormat format) {
        this.location = location; this.format = format;
        uniform(0).glUniformMatrix4f(new org.joml.Matrix4f());
        uniform(64).glUniform4f(1, 1, 1, 1);
        uniform(80).glUniform2i(-1, -1); uniform(88).glUniform2i(-1, -1);
        uniform(120).glUniform1f(1); uniform(128).glUniform1i(1); uniform(132).glUniform1i(1);
    }
    public BCUniform uniform(int offset) { return new BCUniform(this, offset); }
    @SuppressWarnings("unchecked")
    public final T onShaderApplied(Consumer<T> callback) { callbacks.add(callback); return (T)this; }
    @SuppressWarnings("unchecked")
    byte[] snapshot() {
        for (var callback : callbacks) callback.accept((T)this);
        return parameters.array().clone();
    }
    public RenderPipeline pipeline(VertexFormat.Mode mode) {
        return pipelines.computeIfAbsent(mode, m -> {
            boolean energy = location.getPath().equals("energy_bar");
            var builder = RenderPipeline.builder(energy ? RenderPipelines.MATRICES_PROJECTION_SNIPPET : RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                .withLocation(location.withSuffix("_" + mode.name().toLowerCase(Locale.ROOT)))
                .withVertexShader(Identifier.fromNamespaceAndPath(location.getNamespace(), "core/" + (energy || location.getPath().equals("chaos_entity") || location.getPath().equals("position_color_tex_alpha0") ? location.getPath() : "contributor/contrib_base")))
                .withFragmentShader(Identifier.fromNamespaceAndPath(location.getNamespace(), "core/" + location.getPath()))
                .withVertexFormat(format, mode).withCull(false)
                .withUniform("BCParams", UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
            if (!energy) builder.withSampler("Sampler0").withSampler("Sampler1").withSampler("Sampler2").withDepthStencilState(DepthStencilState.DEFAULT);
            return builder.build();
        });
    }
    public RenderType renderType(String name, Identifier texture, VertexFormat.Mode mode) {
        var builder = RenderSetup.builder(pipeline(mode));
        if (texture != null) builder.withTexture("Sampler0", texture);
        if (!location.getPath().equals("energy_bar")) builder.useLightmap().useOverlay();
        var setup = builder.createRenderSetup();
        var type = RenderType.create(name, setup);
        ShaderDraw.bind(type, this, setup);
        return type;
    }
    public final BCUniform getModelMatUniform() { return uniform(0); }
    public final BCUniform getTimeUniform() { return uniform(96); }
    public final BCUniform getDecayUniform() { return uniform(100); }
    public final boolean hasModelMatUniform() { return true; }
    public final boolean hasTimeUniform() { return true; }
    public final boolean hasDecayUniform() { return true; }
}
