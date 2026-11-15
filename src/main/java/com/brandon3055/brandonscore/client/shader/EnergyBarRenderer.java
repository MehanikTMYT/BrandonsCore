package com.brandon3055.brandonscore.client.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

/** One offscreen target per used state; inherited close owns textures/projection buffers. */
public final class EnergyBarRenderer extends PictureInPictureRenderer<EnergyBarState> {
    private static final RenderType TYPE = BCShaders.ENERGY_BAR.renderType("brandonscore/energy_bar", null, VertexFormat.Mode.QUADS);
    public EnergyBarRenderer(MultiBufferSource.BufferSource source) { super(source); }
    public Class<EnergyBarState> getRenderStateClass() { return EnergyBarState.class; }
    protected String getTextureLabel() { return "BrandonsCore energy bar"; }
    protected float getTranslateY(int height, int scale) { return 0; }
    protected void renderToTexture(EnergyBarState state, PoseStack pose) {
        int width = state.x1() - state.x0(), height = state.y1() - state.y0();
        // gl_FragCoord is local to this target. Keep orientation and charge front intact.
        int scale = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
        var shader = BCShaders.ENERGY_BAR;
        shader.getTimeUniform().glUniform1f(state.time());
        shader.uniform(136).glUniform1f(state.charge() * 1.01F);
        shader.uniform(144).glUniform2i(0, 0);
        shader.uniform(152).glUniform2i(width * scale, height * scale);
        shader.uniform(160).glUniform2i(width * scale, height * scale);
        var buffer = bufferSource.getBuffer(ShaderDraw.capture(TYPE));
        var matrix = pose.last().pose();
        float x = -width / 2F;
        buffer.addVertex(matrix, x, height, 0);
        buffer.addVertex(matrix, x + width, height, 0);
        buffer.addVertex(matrix, x + width, 0, 0);
        buffer.addVertex(matrix, x, 0, 0);
    }
}
