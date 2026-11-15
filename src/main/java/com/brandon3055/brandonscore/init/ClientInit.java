package com.brandon3055.brandonscore.init;

import com.brandon3055.brandonscore.client.*;
import com.brandon3055.brandonscore.client.hud.HudManager;
import com.brandon3055.brandonscore.client.model.EquippedItemModelLayer;

import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created by brandon3055 on 15/11/2022
 */
public class ClientInit {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(BCGuiTextures::onResourceReload);
        modBus.addListener(ClientInit::clientSetupEvent);
        modBus.addListener(ClientInit::onAddRenderLayers);
        modBus.addListener(ClientInit::renderStateModifiers);
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent event) ->
            event.register(com.brandon3055.brandonscore.client.shader.EnergyBarState.class,
                com.brandon3055.brandonscore.client.shader.EnergyBarRenderer::new));

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> ContributorHandler.onClientLogin(event.getPlayer()));
        NeoForge.EVENT_BUS.addListener(com.brandon3055.brandonscore.multiblock.MultiBlockManager::onDisconnectFromServer);
        ProcessHandlerClient.init();
        HudManager.init(modBus);
        com.brandon3055.brandonscore.client.shader.BCShaders.init(modBus);
        BCProfiler.init();
        DLRSCache.init();
    }

    private static void renderStateModifiers(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new com.google.common.reflect.TypeToken<LivingEntityRenderer<
                net.minecraft.world.entity.LivingEntity,
                net.minecraft.client.renderer.entity.state.LivingEntityRenderState, ?>>() {},
                (entity, state) -> {
                    state.setRenderData(BCClientRenderState.HIDE_VANILLA_WINGS, ContributorHandler.shouldCancelElytra(entity));
                    if (state instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState humanoid) {
                        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                        if (renderer instanceof LivingEntityRenderer<?, ?, ?> living && living.getModel() instanceof HumanoidModel model) {
                            boolean slim = state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatar
                                && avatar.skin.model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;
                            EquippedItemModelLayer.extract(entity, humanoid, model, slim);
                        }
                    }
                });
    }

    private static void clientSetupEvent(FMLClientSetupEvent event) {
        BCClientEventHandler.init();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void onAddRenderLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            LivingEntityRenderer renderer = event.getPlayerRenderer(skin);
            assert renderer != null;
            renderer.addLayer(new EquippedItemModelLayer(renderer, skin == net.minecraft.world.entity.player.PlayerModelType.SLIM));
        }

        for (var entityType : event.getEntityTypes()) {
            EntityRenderer r = event.getRenderer(entityType);
            if (r instanceof LivingEntityRenderer<?, ?, ?> renderer && renderer.getModel() instanceof HumanoidModel) {
                renderer.addLayer(new EquippedItemModelLayer(renderer, false));
            }
        }
    }
}
