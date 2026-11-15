package com.brandon3055.brandonscore.client.render;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Cuboid6;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.brandonscore.multiblock.MultiBlockPart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.EmptyModel;

import java.util.*;

/**
 * Created by brandon3055 on 25/07/2022
 */
public class MultiBlockRenderers {
    private static final RenderType outlineType = RenderType.create("brandonscore:invalid_outline",
            net.minecraft.client.renderer.rendertype.RenderSetup.builder(
                    com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.LINES_SNIPPET)
                            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("brandonscore", "pipeline/invalid_outline"))
                            .withDepthStencilState(Optional.empty()).build()).createRenderSetup());
    private static final net.minecraft.client.renderer.block.model.BlockDisplayContext DISPLAY_CONTEXT =
            net.minecraft.client.renderer.block.model.BlockDisplayContext.create();

    public static void renderBuildGuide(Level level, BlockPos inWorldOrigin, PoseStack poseStack, SubmitNodeCollector collector, MultiBlockDefinition structure, int packedLight, float partialTicks) {
        Map<BlockPos, MultiBlockPart> blocks = structure.getBlocks();
        Map<MultiBlockPart, BlockState> stateMap = new IdentityHashMap<>();
        Player player = Minecraft.getInstance().player;

        int time = TimeKeeper.getClientTick() % 80;
        int anim = 5;
        if (time >= 70) {
            time -= 70;
            anim += (int) (Math.sin((time / 10F) * Math.PI) * 5);
        }

        List<BlockPos> invalidBlocks = new ArrayList<>();
        for (BlockPos pos : blocks.keySet()) {
            MultiBlockPart part = blocks.get(pos);
            if (part.validBlocks().isEmpty()) continue;
            BlockPos worldPos = inWorldOrigin.offset(pos);

            if (level.isEmptyBlock(worldPos)) {
                if (player != null && worldPos.distToCenterSqr(player.getEyePosition()) < (4*4)) continue;
                BlockState state = stateMap.computeIfAbsent(part, e -> List.copyOf(e.validBlocks()).get((TimeKeeper.getClientTick() / 40) % e.validBlocks().size()).defaultBlockState());
                if (state.isAir()) continue;
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1);
                poseStack.scale(0.8F, 0.8F, 0.8F);
                var modelState = new net.minecraft.client.renderer.block.BlockModelRenderState();
                Minecraft.getInstance().getBlockModelResolver().update(modelState, state, DISPLAY_CONTEXT);
                modelState.submitMultiLayer(poseStack, collector, packedLight, OverlayTexture.pack(anim, 10), 0);
                poseStack.popPose();
            } else if (!part.isMatch(level, worldPos)) {
                invalidBlocks.add(pos);
            }
        }


        if (!invalidBlocks.isEmpty()) {
            for (BlockPos pos : invalidBlocks) {
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                double invalidAnim = Math.max(0.1, Math.abs(Math.sin((TimeKeeper.getClientTick() + partialTicks) / 25D * Math.PI)));
                Cuboid6 box = new Cuboid6().expand(invalidAnim / 2);
                collector.order(1).submitCustomGeometry(poseStack, outlineType, (matrix, buffer) -> {
                    // Lines in 26 carry per-vertex width; the old CCL helper omits it.
                    int[][] edges = {{0,1},{1,3},{3,2},{2,0},{4,5},{5,7},{7,6},{6,4},{0,4},{1,5},{2,6},{3,7}};
                    for (int[] edge : edges) {
                        org.joml.Vector3f[] v = new org.joml.Vector3f[2];
                        for (int i = 0; i < 2; i++) {
                            int corner = edge[i];
                            v[i] = new org.joml.Vector3f((float) ((corner & 1) == 0 ? box.min.x : box.max.x),
                                    (float) ((corner & 4) == 0 ? box.min.y : box.max.y),
                                    (float) ((corner & 2) == 0 ? box.min.z : box.max.z));
                        }
                        var normal = new org.joml.Vector3f(v[1]).sub(v[0]).normalize();
                        for (var vertex : v) buffer.addVertex(matrix, vertex).setColor(1F, 0F, 0F, 1F)
                                .setNormal(matrix, normal).setLineWidth(4F);
                    }
                });
                poseStack.popPose();
            }
        }
    }

}
