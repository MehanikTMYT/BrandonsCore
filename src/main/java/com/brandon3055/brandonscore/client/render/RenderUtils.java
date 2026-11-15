package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;



/**
 * Created by brandon3055 on 16/10/18.
 */
public class RenderUtils {

    public static final net.minecraft.client.renderer.rendertype.RenderType FAN_TYPE =
            net.minecraft.client.renderer.rendertype.RenderType.create("brandonscore:tri_fan_type",
                    net.minecraft.client.renderer.rendertype.RenderSetup.builder(
                            com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_SNIPPET)
                                    .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("brandonscore", "pipeline/tri_fan"))
                                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN).build())
                            .createRenderSetup());

    /**
     * * @return The buffer source used for GUI rendering. You must ALWAYS call endBatch on this when you are done with it.
     */
    @Deprecated
    public static MultiBufferSource.BufferSource getGuiBuffers() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    @Deprecated
    public static MultiBufferSource.BufferSource getBuffers() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    @Deprecated
    public static void endBatch(MultiBufferSource getter) {
        if (getter instanceof MultiBufferSource.BufferSource) {
            ((MultiBufferSource.BufferSource) getter).endBatch();
        }
    }

    public static void drawPieProgress(GuiRender render, double x, double y, double diameter, double progress, double offsetAngle, int colour) {
        drawPieProgress(render, x, y, diameter, progress, offsetAngle, colour, colour);
    }

    public static void drawPieProgress(GuiRender render, double x, double y, double diameter, double progress, double offsetAngle, int innerColour, int outerColour) {
        double radius = diameter / 2;
        double prevAngle = (0.5 - progress) * Math.PI * 2 + MathHelper.torad * offsetAngle;
        for (int i = 1; i <= 30; i++) {
            double angle = ((i / 30D * progress) + 0.5 - progress) * Math.PI * 2 + MathHelper.torad * offsetAngle;
            float cx = (float) (x + radius), cy = (float) (y + radius);
            float ax = (float) (cx + Math.sin(prevAngle) * radius), ay = (float) (cy + Math.cos(prevAngle) * radius);
            float bx = (float) (cx + Math.sin(angle) * radius), by = (float) (cy + Math.cos(angle) * radius);
            // Degenerate quad preserves the original triangle fan and radial tint.
            render.colouredQuad(new float[]{cx, ax, bx, cx}, new float[]{cy, ay, by, cy},
                    new int[]{innerColour, outerColour, outerColour, innerColour});
            prevAngle = angle;
        }
    }
}