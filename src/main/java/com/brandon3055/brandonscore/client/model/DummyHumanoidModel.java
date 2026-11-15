package com.brandon3055.brandonscore.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Intentionally hides the vanilla armor beneath an EquippedModelItem's custom model. */
public class DummyHumanoidModel<T extends HumanoidRenderState> extends HumanoidModel<T> {
    public static final DummyHumanoidModel<?> INSTANCE = new DummyHumanoidModel<>();
    public static final IClientItemExtensions DUMMY_ITEM_RENDER_PROPS = new IClientItemExtensions() {
        @Override
        public Model getGenericArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layer, Model original) {
            return INSTANCE;
        }
    };

    public DummyHumanoidModel() {
        super(createMesh(new CubeDeformation(1), 0).getRoot().bake(64, 64));
        root().visible = false;
    }
}
