package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import java.util.*;

/** Geometry frozen while entity access is legal. Submission retains no entity/model reference. */
public record ExtractedModel(List<Batch> batches) {
    public record Vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float nx, float ny, float nz) {}
    public record Batch(RenderType type, List<Vertex> vertices) {}

    public void submit(PoseStack pose, SubmitNodeCollector collector) {
        for (Batch batch : batches) {
            boolean shader = ShaderDraw.hasParameters(batch.type);
            RenderType type = shader ? ShaderDraw.withPose(batch.type, pose.last().pose()) : batch.type;
            collector.submitCustomGeometry(pose, type, (transform, buffer) -> {
                for (Vertex v : batch.vertices) {
                    if (shader) buffer.addVertex(v.x, v.y, v.z);
                    else buffer.addVertex(transform, v.x, v.y, v.z);
                    buffer.setColor(v.color).setUv(v.u, v.v).setOverlay(v.overlay).setLight(v.light);
                    if (shader) buffer.setNormal(v.nx, v.ny, v.nz);
                    else buffer.setNormal(transform, v.nx, v.ny, v.nz);
                }
            });
        }
    }
    public static final class Capture implements MultiBufferSource {
        private final List<Builder> batches = new ArrayList<>();
        public VertexConsumer getBuffer(RenderType type) {
            Builder builder = new Builder(ShaderDraw.capture(type));
            batches.add(builder);
            return builder;
        }
        public ExtractedModel finish() {
            return new ExtractedModel(batches.stream().map(Builder::finish).toList());
        }
    }
    private static final class Builder implements VertexConsumer {
        private final RenderType type;
        private final List<Vertex> vertices = new ArrayList<>();
        private boolean pending;
        private float x,y,z,u,v,nx,ny,nz;
        private int color=-1,overlay,light;
        Builder(RenderType type) { this.type=type; }
        private void flush() {
            if (pending) vertices.add(new Vertex(x,y,z,color,u,v,overlay,light,nx,ny,nz));
            pending=false;
        }
        Batch finish() { flush(); return new Batch(type, List.copyOf(vertices)); }
        public VertexConsumer addVertex(float x,float y,float z) { flush(); this.x=x;this.y=y;this.z=z;pending=true;return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { return setColor(a<<24 | r<<16 | g<<8 | b); }
        public VertexConsumer setColor(int color) { this.color=color;return this; }
        public VertexConsumer setUv(float u,float v) { this.u=u;this.v=v;return this; }
        public VertexConsumer setUv1(int u,int v) { overlay=u | v<<16;return this; }
        public VertexConsumer setUv2(int u,int v) { light=u | v<<16;return this; }
        public VertexConsumer setNormal(float x,float y,float z) { nx=x;ny=y;nz=z;return this; }
        public VertexConsumer setLineWidth(float width) { throw new IllegalArgumentException("Line geometry requires a line extraction state"); }
    }
}
