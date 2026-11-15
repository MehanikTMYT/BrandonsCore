package com.brandon3055.brandonscore.client.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.rendertype.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import java.nio.*;
import java.util.*;

/** Parameter ownership follows one extracted batch, never a global current shader. */
public final class ShaderDraw {
    private record Binding(BCShader<?> shader, RenderSetup setup) {}
    private static final Map<RenderType, Binding> bindings = new WeakHashMap<>();
    private static final Map<RenderType, byte[]> snapshots = new WeakHashMap<>();
    static void bind(RenderType type, BCShader<?> shader, RenderSetup setup) { bindings.put(type, new Binding(shader, setup)); }
    public static RenderType snapshot(RenderType type) {
        var binding = bindings.get(type);
        if (binding == null) return type;
        var frozen = RenderType.create("brandonscore/extracted", binding.setup);
        snapshots.put(frozen, binding.shader.snapshot());
        return frozen;
    }
    public static boolean hasParameters(RenderType type) { return snapshots.containsKey(type); }
    public static RenderType withPose(RenderType type, Matrix4f pose) {
        byte[] data = snapshots.get(type);
        if (data == null) return type;
        byte[] copy = data.clone();
        ByteBuffer block = ByteBuffer.wrap(copy).order(ByteOrder.nativeOrder());
        BCUniform.writeMatrix(block, 0, new Matrix4f(pose).mul(BCUniform.readMatrix(block, 0)));
        // The existing setup is immutable; its textures and pipeline are retained.
        var frozen = RenderType.create("brandonscore/posed", setup(type));
        snapshots.put(frozen, copy);
        return frozen;
    }
    private static final Map<RenderType, RenderSetup> setups = new WeakHashMap<>();
    private static RenderSetup setup(RenderType type) { return Objects.requireNonNull(setups.get(type)); }
    public static RenderType capture(RenderType type) {
        RenderType frozen = snapshot(type);
        var binding = bindings.get(type);
        if (binding != null) setups.put(frozen, binding.setup);
        return frozen;
    }
    /** A draw owns its upload until commands have been submitted; close releases the backend resource. */
    public static void draw(RenderType type, RenderPass pass, int baseVertex, int firstIndex, int count, int instances) {
        byte[] data = snapshots.get(type);
        if (data == null) { pass.drawIndexed(baseVertex, firstIndex, count, instances); return; }
        ByteBuffer upload = MemoryUtil.memAlloc(data.length);
        try {
            upload.put(data).flip();
            try (GpuBuffer uniform = RenderSystem.getDevice().createBuffer(() -> "BrandonsCore parameters", GpuBuffer.USAGE_UNIFORM, upload)) {
                pass.setUniform("BCParams", uniform);
                pass.drawIndexed(baseVertex, firstIndex, count, instances);
            }
        } finally { MemoryUtil.memFree(upload); }
    }
}
