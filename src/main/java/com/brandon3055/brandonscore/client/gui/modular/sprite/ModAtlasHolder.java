/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 20/08/2023
 */
public class ModAtlasHolder implements PreparableReloadListener, AutoCloseable {

    private final TextureAtlas textureAtlas;
    private final Identifier atlasLocation;
    private final Identifier atlasInfoLocation;
    private final String modid;

    /**
     * Defines a mod texture atlas.
     * Must be registered as a resource reload listener via RegisterClientReloadListenersEvent
     * This is all that is needed to create a custom texture atlas.
     *
     * @param modid             The mod id of the mod registering this atlas.
     * @param atlasLocation     The texture atlas location. e.g. "textures/atlas/gui.png" (Will have the modid: prefix added automatically)
     * @param atlasInfoLocation The path to the atlas json file relative to modid:atlases/
     *                          e.g. "gui" will point to modid:atlases/gui.json
     */
    public ModAtlasHolder(String modid, String atlasLocation, String atlasInfoLocation) {
        this.atlasInfoLocation = Identifier.fromNamespaceAndPath(modid, atlasInfoLocation);
        this.atlasLocation = Identifier.fromNamespaceAndPath(modid, atlasLocation);
        this.textureAtlas = new TextureAtlas(this.atlasLocation);
        this.modid = modid;
    }

    public void init() {
        Minecraft.getInstance().getTextureManager().register(this.textureAtlas.location(), this.textureAtlas);
    }

    public Identifier atlasLocation() {
        return atlasLocation;
    }

    public TextureAtlasSprite getSprite(Identifier resourceLocation) {
        return this.textureAtlas.getSprite(resourceLocation);
    }

    @Override
    public final @NotNull CompletableFuture<Void> reload(SharedState state, Executor prepareExecutor,
            PreparationBarrier barrier, Executor applyExecutor) {
        SpriteLoader loader = SpriteLoader.create(textureAtlas);
        return loader.loadAndStitch(new ModResourceManager(state.resourceManager(), modid),
                        atlasInfoLocation, 0, prepareExecutor, Set.of(net.minecraft.client.resources.metadata.gui.GuiMetadataSection.TYPE))
                .thenCompose(preparations -> preparations.readyForUpload().thenApply(unused -> preparations))
                .thenCompose(barrier::wait)
                .thenAcceptAsync(textureAtlas::upload, applyExecutor);
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }

    public static class ModResourceManager implements ResourceManager {

        private final ResourceManager wrapped;
        private final String modid;

        public ModResourceManager(ResourceManager wrapped, String modid) {
            this.wrapped = wrapped;
            this.modid = modid;
        }

        @Override
        public Map<Identifier, Resource> listResources(String pPath, Predicate<Identifier> pFilter) {
            return wrapped.listResources(pPath, pFilter.and(e -> e.getNamespace().equals(modid)));
        }

        //@formatter:off
        @Override public Set<String> getNamespaces() { return wrapped.getNamespaces(); }
        @Override public List<Resource> getResourceStack(Identifier pLocation) { return wrapped.getResourceStack(pLocation); }
        @Override public Map<Identifier, List<Resource>> listResourceStacks(String pPath, Predicate<Identifier> pFilter) { return wrapped.listResourceStacks(pPath, pFilter); }
        @Override public Stream<PackResources> listPacks() { return wrapped.listPacks(); }
        @Override public Optional<Resource> getResource(Identifier pLocation) { return wrapped.getResource(pLocation); }
        //@formatter:on
    }
}
