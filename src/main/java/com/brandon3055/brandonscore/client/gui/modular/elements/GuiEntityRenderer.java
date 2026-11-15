/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.elements;

import com.brandon3055.brandonscore.client.gui.modular.lib.BackgroundRender;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.GuiParent;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.Rectangle;
import codechicken.lib.render.CCRenderEventHandler;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 15/11/2023
 */
public class GuiEntityRenderer extends GuiElement<GuiEntityRenderer> implements BackgroundRender {

    public static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Identifier, Entity> entityCache = new HashMap<>();
    private static final List<Identifier> invalidEntities = new ArrayList<>();

    private Supplier<Float> rotationSpeed = () -> 1F;
    private Supplier<Float> lockedRotation = () -> 0F;
    private Entity entity;
    private Identifier entityName;
    private boolean invalidEntity = false;
    private Supplier<Boolean> rotationLocked = () -> false;
    private Supplier<Boolean> trackMouse = () -> false;
    private Supplier<Boolean> drawName = () -> false;
    public boolean force2dSize = false;

    public GuiEntityRenderer(@NotNull GuiParent<?> parent) {
        super(parent);
    }

    public GuiEntityRenderer setEntity(Entity entity) {
        this.entity = entity;
        if (this.entity == null) {
            invalidEntity = true;
            return this;
        }

        this.entityName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        invalidEntity = invalidEntities.contains(entityName);
        return this;
    }

    public GuiEntityRenderer setEntity(Identifier entity) {
        this.entityName = entity;
        this.entity = entityCache.computeIfAbsent(entity, resourceLocation -> {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entity).orElse(null);
            return type == null ? null : type.create(mc().level, net.minecraft.world.entity.EntitySpawnReason.LOAD);
        });

        invalidEntity = this.entity == null;
        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    public GuiEntityRenderer setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = () -> rotationSpeed;
        return this;
    }

    public GuiEntityRenderer setRotationSpeed(Supplier<Float> rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public float getRotationSpeed() {
        return rotationSpeed.get();
    }

    public GuiEntityRenderer setLockedRotation(float lockedRotation) {
        this.lockedRotation = () -> lockedRotation;
        return this;
    }

    public GuiEntityRenderer setLockedRotation(Supplier<Float> lockedRotation) {
        this.lockedRotation = lockedRotation;
        return this;
    }

    public float getLockedRotation() {
        return lockedRotation.get();
    }

    public GuiEntityRenderer setRotationLocked(boolean rotationLocked) {
        this.rotationLocked = () -> rotationLocked;
        return this;
    }

    public GuiEntityRenderer setRotationLocked(Supplier<Boolean> rotationLocked) {
        this.rotationLocked = rotationLocked;
        return this;
    }

    public boolean isRotationLocked() {
        return rotationLocked.get();
    }

    public GuiEntityRenderer setTrackMouse(boolean trackMouse) {
        this.trackMouse = () -> trackMouse;
        return this;
    }

    public GuiEntityRenderer setTrackMouse(Supplier<Boolean> trackMouse) {
        this.trackMouse = trackMouse;
        return this;
    }

    public boolean isTrackMouse() {
        return trackMouse.get();
    }

    public GuiEntityRenderer setDrawName(boolean drawName) {
        this.drawName = () -> drawName;
        return this;
    }

    public GuiEntityRenderer setDrawName(Supplier<Boolean> drawName) {
        this.drawName = drawName;
        return this;
    }

    public boolean isDrawName() {
        return drawName.get();
    }

    public GuiEntityRenderer setForce2dSize(boolean force2dSize) {
        this.force2dSize = force2dSize;
        return this;
    }

    @Override
    public double getBackgroundDepth() {
        Rectangle rect = getRectangle();
        if (invalidEntity || entity == null) return 0.01;
        float scale = (float) (force2dSize ? (Math.min(rect.height() / entity.getBbHeight(), rect.width() / entity.getBbWidth())) : rect.height() / entity.getBbHeight());
        if (Float.isInfinite(scale)) scale = 1;
        return scale * 2;
    }

    @Override
    public void renderBackground(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        if (invalidEntity) return;

        try {
            if (entity != null) {
                Rectangle rect = getRectangle();
                float scale = (float) (force2dSize ? (Math.min(rect.height() / entity.getBbHeight(), rect.width() / entity.getBbWidth())) : rect.height() / entity.getBbHeight());
                float xPos = (float) (rect.x() + (rect.width() / 2D));
                float yPos = (float) ((yMin() + (ySize() / 2)) + (rect.height() / 2));
                float rotation = rotationLocked.get() ? lockedRotation.get() : (CCRenderEventHandler.renderTime + partialTicks) * rotationSpeed.get();
                if (entity instanceof LivingEntity living) {
                    int eyeOffset = (int) ((entity.getEyeHeight()) * scale);
                    if (trackMouse.get()) {
                        renderEntityInInventoryFollowsMouse(render, xPos, yPos, scale, force2dSize, xPos - (float) mouseX, yPos - (float) mouseY - eyeOffset, living);
                    } else {
                        renderEntityInInventoryWithRotation(render, xPos, yPos, scale, force2dSize, rotation, living);
                    }
                } else {
                    Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
                    Quaternionf quaternionf1 = Axis.YP.rotationDegrees(rotation);
                    quaternionf.mul(quaternionf1);
                    renderEntityInInventory(render, xPos, yPos, scale, force2dSize, quaternionf, quaternionf1, entity);
                }
            }
        } catch (Throwable e) {
            invalidEntity = true;
            invalidEntities.add(entityName);
            LOGGER.error("Failed to render entity in GUI. This is not a bug there are just some entities that can not be rendered like this.");
            LOGGER.error("Entity: " + entity, e);
        }
    }

    public static void renderEntityInInventoryFollowsMouse(GuiRender render, double pX, double pY, double pScale, float offsetX, float offsetY, LivingEntity pEntity) {
        renderEntityInInventoryFollowsMouse(render, pX, pY, pScale, false, offsetX, offsetY, pEntity);
    }

    public static void renderEntityInInventoryFollowsMouse(GuiRender render, double pX, double pY, double pScale, boolean flat, float offsetX, float offsetY, LivingEntity pEntity) {
        float xAngle = (float) Math.atan(offsetX / 40.0F);
        float yAngle = (float) Math.atan(offsetY / 40.0F);
        renderEntityInInventoryFollowsAngle(render, pX, pY, pScale, flat, xAngle, yAngle, pEntity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiRender render, double pX, double pY, double pScale, float angleX, float angleY, LivingEntity pEntity) {
        renderEntityInInventoryFollowsAngle(render, pX, pY, pScale, false, angleX, angleY, pEntity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiRender render, double pX, double pY, double pScale, boolean flat, float angleX, float angleY, LivingEntity pEntity) {
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleY * 20.0F * ((float) Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = pEntity.yBodyRot;
        float f3 = pEntity.getYRot();
        float f4 = pEntity.getXRot();
        float f5 = pEntity.yHeadRotO;
        float f6 = pEntity.yHeadRot;
        pEntity.yBodyRot = 180.0F + angleX * 20.0F;
        pEntity.setYRot(180.0F + angleX * 40.0F);
        pEntity.setXRot(-angleY * 20.0F);
        pEntity.yHeadRot = pEntity.getYRot();
        pEntity.yHeadRotO = pEntity.getYRot();
        renderEntityInInventory(render, pX, pY, pScale, flat, quaternionf, quaternionf1, pEntity);
        pEntity.yBodyRot = f2;
        pEntity.setYRot(f3);
        pEntity.setXRot(f4);
        pEntity.yHeadRotO = f5;
        pEntity.yHeadRot = f6;
    }

    public static void renderEntityInInventoryWithRotation(GuiRender render, double xPos, double yPos, double scale, double rotation, LivingEntity living) {
        renderEntityInInventoryWithRotation(render, xPos, yPos, scale, false, rotation, living);
    }

    public static void renderEntityInInventoryWithRotation(GuiRender render, double xPos, double yPos, double scale, boolean flat, double rotation, LivingEntity living) {
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = Axis.YP.rotationDegrees((float) rotation);
        quaternionf.mul(quaternionf1);
        float f2 = living.yBodyRot;
        float f3 = living.getYRot();
        float f4 = living.getXRot();
        float f5 = living.yHeadRotO;
        float f6 = living.yHeadRot;
        living.yBodyRot = 180.0F;
        living.setYRot(180.0F);
        living.setXRot(0);
        living.yHeadRot = living.getYRot();
        living.yHeadRotO = living.getYRot();
        renderEntityInInventory(render, xPos, yPos, scale, flat, quaternionf, quaternionf1, living);
        living.yBodyRot = f2;
        living.setYRot(f3);
        living.setXRot(f4);
        living.yHeadRotO = f5;
        living.yHeadRot = f6;
    }

    public static void renderEntityInInventory(GuiRender render, double pX, double pY, double pScale, Quaternionf quat, @Nullable Quaternionf pCameraOrientation, Entity pEntity) {
        renderEntityInInventory(render, pX, pY, pScale, false, quat, pCameraOrientation, pEntity);
    }

    public static void renderEntityInInventory(GuiRender render, double pX, double pY, double pScale, boolean flat, Quaternionf quat, @Nullable Quaternionf pCameraOrientation, Entity pEntity) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(pEntity);
        var state = renderer.createRenderState(pEntity, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        // A PIP state captures the entity now; later rendering never mutates the live entity.
        // The viewport is centred on the model, with its feet at pY.
        render.withExtraction(() -> {
            var graphics = render.guiGraphicsWrapper();
            org.joml.Vector2f feet = graphics.pose().transformPosition((float) pX, (float) pY, new org.joml.Vector2f());
            float transformScale = (float) Math.hypot(graphics.pose().m10(), graphics.pose().m11());
            float scale = (float) pScale * transformScale;
            int halfWidth = Math.max(1, (int) Math.ceil(Math.max(pEntity.getBbWidth(), pEntity.getBbHeight()) * scale));
            int height = Math.max(1, (int) Math.ceil(pEntity.getBbHeight() * scale));
            graphics.entity(state, scale, new org.joml.Vector3f(0, pEntity.getBbHeight() / 2, 0),
                    new Quaternionf(quat), pCameraOrientation == null ? null : new Quaternionf(pCameraOrientation).conjugate(),
                    (int) feet.x - halfWidth, (int) feet.y - height, (int) feet.x + halfWidth, (int) feet.y);
        });
    }
}
