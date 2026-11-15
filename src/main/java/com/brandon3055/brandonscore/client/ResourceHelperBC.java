package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by brandon3055 on 29/3/2016.
 * Helper function to bind and cache resource locations.
 */
@Deprecated
public class ResourceHelperBC {

    private static Identifier vanillaParticles;
    private static Map<String, Identifier> cachedResources = new HashMap<String, Identifier>();
    public static final String RESOURCE_PREFIX = BrandonsCore.MODID.toLowerCase(Locale.ENGLISH) + ":";

    public static net.minecraft.client.gui.render.TextureSetup textureSetup(Identifier texture) {
        var loaded = Minecraft.getInstance().getTextureManager().getTexture(texture);
        return net.minecraft.client.gui.render.TextureSetup.singleTexture(loaded.getTextureView(), loaded.getSampler());
    }

    public static Identifier getResource(String rs) {
        if (!cachedResources.containsKey(rs)) cachedResources.put(rs, Identifier.parse(RESOURCE_PREFIX + rs));
        return cachedResources.get(rs);
    }

    public static Identifier getResourceRAW(String rs) {
        if (!cachedResources.containsKey(rs)) cachedResources.put(rs, Identifier.parse(rs));
        return cachedResources.get(rs);
    }

    public static net.minecraft.client.gui.render.TextureSetup textureSetup(String rs) {
        return textureSetup(getResource(rs));
    }
}
