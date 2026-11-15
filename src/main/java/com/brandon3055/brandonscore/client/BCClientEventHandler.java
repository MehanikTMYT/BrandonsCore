package com.brandon3055.brandonscore.client;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IFOVModifierItem;
import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.*;


/**
 * Created by brandon3055 on 17/07/2016.
 */
public class BCClientEventHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    private static int remountTicksRemaining = 0;
    private static int remountEntityID = 0;
    private static int debugTimeout = 0;
    private static Map<ResourceKey<Level>, Integer[]> dimTickTimes = new HashMap<>();
    private static Integer[] overallTickTime = new Integer[200];
    private static int renderIndex = 0;
    private static LinkedList<ResourceKey<Level>> sortingOrder = new LinkedList<>();
    public static int elapsedTicks = 0;

    private static Comparator<ResourceKey<Level>> sorter = (value, compare) -> {
        long totalValue = 0;
        for (Integer time : dimTickTimes.get(value)) {
            totalValue += time;
        }
        totalValue /= 200;

        long totalCompare = 0;
        for (Integer time : dimTickTimes.get(compare)) {
            totalCompare += time;
        }
        totalCompare /= 200;

        return Long.compare(totalCompare, totalValue);
    };

    public static void init() {
        LOCK.lock();

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new BCClientEventHandler());
    }

    @SubscribeEvent
    public void disconnectEvent(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            BCEventHandler.noClipPlayers.remove(mc.player.getUUID());
        }
    }

    @SubscribeEvent
    public void tickEnd(ClientTickEvent.Post event) {

        elapsedTicks++;
        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        if (debugTimeout > 0) {
            debugTimeout--;
        }

        if (elapsedTicks % 100 == 0 && debugTimeout > 0) {
            sortingOrder.clear();
            sortingOrder.addAll(dimTickTimes.keySet());
            sortingOrder.sort(sorter);
        }
    }

    @SubscribeEvent
    public void drawSelectionEvent(ExtractBlockOutlineRenderStateEvent event) {
        Level level = Minecraft.getInstance().level;
        if (event.getHitResult().getType() == HitResult.Type.MISS || level == null) return;
        BlockState state = level.getBlockState(event.getHitResult().getBlockPos());
        if (state.getBlock() instanceof BlockBCore block) {
            if (!block.renderSelectionBox(event, level)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fovUpdate(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        float originalFOV = event.getFovModifier();
        float newFOV = originalFOV;

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
                newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, slot);
            }
        }

        ItemStack stack = player.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EquipmentSlot.OFFHAND);
        }
        stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EquipmentSlot.MAINHAND);
        }

        if (newFOV != originalFOV) {
            event.setNewFovModifier(newFOV);
        }
    }

    private static final net.minecraft.util.context.ContextKey<List<net.minecraft.world.phys.Vec3>> DEBUG_BOXES =
            new net.minecraft.util.context.ContextKey<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("brandonscore", "debug_boxes"));

    @SubscribeEvent
    public void extractDebug(ExtractLevelRenderStateEvent event) {
        List<Vec3> boxes = debugBlockList == null ? List.of() : debugBlockList.stream()
                .map(v -> new Vec3(v.x, v.y, v.z))
                .sorted(Comparator.comparingDouble(v -> v.distanceToSqr(event.getCamera().position())))
                .limit(1001).toList();
        event.getRenderState().setRenderData(DEBUG_BOXES, boxes);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderLevelStage(SubmitCustomGeometryEvent event) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var levelState = event.getLevelRenderState();
        var camera = levelState.cameraRenderState;
        PoseStack pose = event.getPoseStack();
        // Vanilla extraction already includes visible and global block entities,
        // validity, view-distance, and frustum checks. Do not walk live chunk lists.
        for (var state : levelState.blockEntityRenderStates) {
            var renderer = dispatcher.getRenderer(state);
            if (renderer instanceof BlockEntityRendererTransparent<?, ?> transparent) {
                pose.pushPose();
                var pos = state.blockPos;
                pose.translate(pos.getX() - camera.pos.x, pos.getY() - camera.pos.y, pos.getZ() - camera.pos.z);
                submitTransparent(transparent, state, pose, event.getSubmitNodeCollector(), camera);
                pose.popPose();
            }
        }
        doDebugRendering(event);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void submitTransparent(BlockEntityRendererTransparent renderer,
            net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        renderer.submitTransparent(state, pose, collector, camera);
    }

    private static final RenderType boxNoDepth = RenderType.create("brandonscore:box_no_depth",
            net.minecraft.client.renderer.rendertype.RenderSetup.builder(
                    com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("brandonscore", "pipeline/box_no_depth"))
                            .withDepthStencilState(java.util.Optional.empty()).build())
                    .sortOnUpload().createRenderSetup());

    private static final Cuboid6 BOX = Cuboid6.full.copy().expand(0.02);
    private static final float[] RED = EnumColour.RED.getColour(128).packArray();
    public static List<Vector3> debugBlockList = null;

    public void doDebugRendering(SubmitCustomGeometryEvent event) {
        var boxes = event.getLevelRenderState().getRenderData(DEBUG_BOXES);
        if (boxes == null || boxes.isEmpty()) return;
        var cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        event.getSubmitNodeCollector().submitCustomGeometry(pose, boxNoDepth, (matrix, buffer) -> {
            var consumer = new TransformingVertexConsumer(buffer, new codechicken.lib.vec.Matrix4(matrix));
            for (Vec3 pos : boxes) RenderUtils.bufferCuboidSolid(consumer,
                    BOX.copy().add(new Vector3(pos.x, pos.y, pos.z)), RED[0], RED[1], RED[2], RED[3]);
        });
        pose.popPose();
    }
}
