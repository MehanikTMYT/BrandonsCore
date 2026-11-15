package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.vec.Matrix4;
import java.nio.ByteBuffer;
import java.util.Objects;
import org.joml.Matrix4f;

/** CPU-side parameter writer. Values are copied into an immutable std140 block at extraction. */
public final class BCUniform {
    private final BCShader<?> owner;
    private final int offset;
    BCUniform(BCShader<?> owner, int offset) { this.owner = owner; this.offset = offset; }
    public void glUniform1f(float v) { owner.parameters.putFloat(offset, v); }
    public void glUniform1i(int v) { owner.parameters.putInt(offset, v); }
    public void glUniform2i(int x, int y) { owner.parameters.putInt(offset, x).putInt(offset + 4, y); }
    public void glUniform4f(float x, float y, float z, float w) {
        owner.parameters.putFloat(offset, x).putFloat(offset + 4, y).putFloat(offset + 8, z).putFloat(offset + 12, w);
    }
    public void glUniformMatrix4f(Matrix4 m) { glUniformMatrix4f(m.toMatrix4f()); }
    public void glUniformMatrix4f(Matrix4f m) { writeMatrix(owner.parameters, offset, m); }

    /** std140 mat4: four columns of four floats. Never pass heap buffers to JOML's native fast path. */
    static void writeMatrix(ByteBuffer block, int offset, Matrix4f matrix) {
        Objects.checkFromIndexSize(offset, 64, block.limit());
        float[] values = matrix.get(new float[16]);
        for (int i = 0; i < values.length; i++) block.putFloat(offset + i * Float.BYTES, values[i]);
    }

    /** Absolute reads preserve the buffer position and honor its byte order, just like writeMatrix. */
    static Matrix4f readMatrix(ByteBuffer block, int offset) {
        Objects.checkFromIndexSize(offset, 64, block.limit());
        float[] values = new float[16];
        for (int i = 0; i < values.length; i++) values[i] = block.getFloat(offset + i * Float.BYTES);
        return new Matrix4f().set(values);
    }
}
