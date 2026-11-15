package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/** Downloads off-thread, then replaces the placeholder on the render thread. */
public class ThreadedImageDownloader extends SimpleTexture {
    private final File cacheFile;
    private final String imageUrl;
    private DLResourceLocation dlLocation;
    private CompletableFuture<?> future;
    private boolean applying;
    private boolean released;

    public ThreadedImageDownloader(File cacheFile, String imageUrl, DLResourceLocation placeholder) {
        super(placeholder.location());
        this.cacheFile = cacheFile;
        this.imageUrl = imageUrl;
    }

    public void setDlLocation(DLResourceLocation location) { this.dlLocation = location; }

    @Override
    public TextureContents loadContents(ResourceManager manager) throws IOException {
        if (cacheFile != null && cacheFile.isFile()) {
            try (InputStream stream = Files.newInputStream(cacheFile.toPath())) {
                NativeImage image = NativeImage.read(stream);
                updateSize(image);
                return new TextureContents(image, null);
            } catch (IOException error) {
                LogHelperBC.logger.warn("Invalid cached image {}", cacheFile, error);
            }
        }
        return super.loadContents(manager);
    }

    public void start() {
        if (future != null || (dlLocation != null && dlLocation.dlFinished)) return;
        future = CompletableFuture.runAsync(() -> {
            HttpURLConnection connection = null;
            try {
                connection = FileHandler.openConnection(imageUrl, Minecraft.getInstance().getProxy());
                connection.connect();
                if (connection.getResponseCode() / 100 != 2) throw new IOException("HTTP " + connection.getResponseCode());
                byte[] bytes;
                try (InputStream stream = connection.getInputStream()) { bytes = stream.readAllBytes(); }
                NativeImage image = NativeImage.read(new java.io.ByteArrayInputStream(bytes));
                if (cacheFile != null) {
                    Files.createDirectories(cacheFile.toPath().getParent());
                    Files.write(cacheFile.toPath(), bytes);
                }
                Minecraft.getInstance().execute(() -> {
                    if (released) { image.close(); return; }
                    updateSize(image);
                    apply(new TextureContents(image, null));
                });
            } catch (Exception error) {
                LogHelperBC.logger.warn("Couldn't download image {}", imageUrl, error);
                if (dlLocation != null) dlLocation.dlFailed = dlLocation.dlFinished = true;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, Util.backgroundExecutor());
    }

    private void updateSize(NativeImage image) {
        if (dlLocation == null) return;
        dlLocation.width = image.getWidth();
        dlLocation.height = image.getHeight();
        dlLocation.sizeSet = true;
        dlLocation.dlFailed = false;
        dlLocation.dlFinished = true;
    }

    @Override
    public void apply(TextureContents contents) {
        applying = true;
        try {
            super.apply(contents);
        } finally {
            applying = false;
        }
    }

    @Override
    public void close() {
        // ReloadableTexture.doLoad calls close while replacing its own GPU allocation.
        if (!applying) released = true;
        super.close();
    }
}
