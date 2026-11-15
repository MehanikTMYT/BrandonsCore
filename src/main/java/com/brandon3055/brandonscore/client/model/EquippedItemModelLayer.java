package com.brandon3055.brandonscore.client.model;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.render.ExtractedModel;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.items.EquippedModelItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

/** Live equipment and animations are resolved during extraction, before collector submission. */
public class EquippedItemModelLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    public static final ContextKey<ExtractedModel> GEOMETRY = new ContextKey<>(Identifier.fromNamespaceAndPath("brandonscore", "equipped_geometry"));
    private static ContributorModel<?> contributor;
    public EquippedItemModelLayer(RenderLayerParent<S,M> renderer, boolean slimModel) { super(renderer); }

    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        var geometry = state.getRenderData(GEOMETRY);
        if (geometry != null) geometry.submit(pose, collector);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void extract(LivingEntity entity, HumanoidRenderState state, HumanoidModel parent, boolean slim) {
        parent.setupAnim(state);
        float partial = state.ageInTicks - entity.tickCount;
        var capture = new ExtractedModel.Capture();
        var pose = new PoseStack();
        for (var slot : new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD}) {
            renderItem(entity, entity.getItemBySlot(slot), slot, parent, slim, pose, capture, state.lightCoords, partial);
        }
        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e -> e.getItem() instanceof EquippedModelItem, entity);
            if (!stack.isEmpty()) renderItem(entity, stack, null, parent, slim, pose, capture, state.lightCoords, partial);
        }
        if (entity instanceof Player player) {
            var props = ContributorHandler.getProps(player);
            if (props.isContributor()) {
                if (contributor == null) contributor = new ContributorModel<>();
                net.neoforged.neoforge.client.ClientHooks.copyModelProperties(parent, contributor);
                contributor.props = props;
                contributor.render(entity, pose, capture, ItemStack.EMPTY, state.lightCoords, OverlayTexture.NO_OVERLAY, partial);
            }
        }
        state.setRenderData(GEOMETRY, capture.finish());
    }
    private static void renderItem(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> parent,
            boolean slim, PoseStack pose, ExtractedModel.Capture capture, int light, float partial) {
        if (!(stack.getItem() instanceof EquippedModelItem item) || !item.shouldRenderInSlot(entity, stack, slot)) return;
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (slot != null && equippable != null && equippable.slot() != slot) return;
        var model = item.getExtendedModel(entity, stack, slot, parent, slim);
        model.render(entity, pose, capture, stack.copy(), light, OverlayTexture.NO_OVERLAY, partial);
    }
}
