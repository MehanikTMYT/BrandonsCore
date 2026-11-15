package com.brandon3055.brandonscore.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Extra translucent geometry, extracted into the renderer's normal immutable state. */
public interface BlockEntityRendererTransparent<T extends BlockEntity, S extends BlockEntityRenderState> extends BlockEntityRenderer<T, S> {
    void submitTransparent(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera);
}
