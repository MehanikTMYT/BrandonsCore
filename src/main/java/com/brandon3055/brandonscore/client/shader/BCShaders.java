package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.math.MathHelper;


import codechicken.lib.util.ClientUtils;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.IEventBus;


/**
 * Created by brandon3055 on 15/05/2022
 */
public class BCShaders {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    public static final ChaosEntityShader CHAOS_ENTITY_SHADER = new ChaosEntityShader("chaos_entity", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> {
                Player player = Minecraft.getInstance().player;
                e.getTimeUniform().glUniform1f((float) ClientUtils.getRenderTime());
                e.getYawUniform().glUniform1f((float) ((player == null ? 0 : player.getYRot()) * MathHelper.torad));
                e.getPitchUniform().glUniform1f((float) -((player == null ? 0 : player.getXRot()) * MathHelper.torad));
            });

    public static final ContribShader CONTRIB_BASE_SHADER = new ContribShader("contributor/contrib_base", DefaultVertexFormat.ENTITY);
    public static final ContribShader WINGS_WEB_SHADER = new ContribShader("contributor/wings_web", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader WINGS_BONE_SHADER = new ContribShader("contributor/wings_bone", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader VET_BADGE_SHADER = new ContribShader("contributor/vet_badge", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));

    public static final ContribShader BADGE_OUTLINE_SHADER = new ContribShader("contributor/badge_outline", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_CORE_SHADER = new ContribShader("contributor/patreon_core", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 20)));
    public static final ContribShader BADGE_FOIL_SHADER = new ContribShader("contributor/badge_foil", DefaultVertexFormat.ENTITY)
            .onShaderApplied(e -> e.getTimeUniform().glUniform1f((float) (ClientUtils.getRenderTime() / 40)));

    public static final ContribShader ENERGY_BAR = new ContribShader("energy_bar", DefaultVertexFormat.POSITION);
    public static final ContribShader POS_COLOUR_TEX_ALPHA0 = new ContribShader("position_color_tex_alpha0", DefaultVertexFormat.POSITION_TEX_COLOR);

    public static void init(IEventBus bus) {
        LOCK.lock();
        bus.addListener((net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) -> {
            for (var shader : new BCShader<?>[]{CHAOS_ENTITY_SHADER, CONTRIB_BASE_SHADER, WINGS_WEB_SHADER,
                    WINGS_BONE_SHADER, VET_BADGE_SHADER, BADGE_OUTLINE_SHADER, BADGE_CORE_SHADER, BADGE_FOIL_SHADER}) {
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS));
                event.registerPipeline(shader.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES));
            }
            event.registerPipeline(ENERGY_BAR.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS));
            event.registerPipeline(POS_COLOUR_TEX_ALPHA0.pipeline(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS));
        });
    }
}
