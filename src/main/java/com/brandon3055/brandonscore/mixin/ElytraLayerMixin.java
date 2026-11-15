package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import net.covers1624.quack.util.SneakyUtils;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by brandon3055 on 4/2/21
 */
@Mixin(net.minecraft.client.renderer.entity.layers.WingsLayer.class)
public class ElytraLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"), cancellable = true)
    private void bc$hideContributorWings(com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector, int light,
            net.minecraft.client.renderer.entity.state.HumanoidRenderState state,
            float yaw, float pitch, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (Boolean.TRUE.equals(state.getRenderData(
                com.brandon3055.brandonscore.client.BCClientRenderState.HIDE_VANILLA_WINGS))) ci.cancel();
    }
}
