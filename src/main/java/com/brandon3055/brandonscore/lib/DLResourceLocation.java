package com.brandon3055.brandonscore.lib;

import net.minecraft.resources.Identifier;

/**
 * Created by brandon3055 on 13/09/2016.
 *
 */
public class DLResourceLocation {

    private final Identifier location;
    public volatile int width = 0;
    public volatile int height = 0;
    public volatile boolean sizeSet = false;
    public volatile boolean dlFailed = false;
    public volatile boolean dlFinished = false;
    public boolean lastCheckStatus = false;

    public DLResourceLocation(String resourceDomainIn, String url) {
        this.location = Identifier.fromNamespaceAndPath(resourceDomainIn, url);
    }

    public Identifier location() { return location; }

    @Override
    public String toString() { return location.toString(); }

    /**
     * @return true once when the download completes
     */
    public boolean dlStateChanged() {
        if (dlFinished && !lastCheckStatus) {
            lastCheckStatus = true;
            return true;
        }
        return false;
    }
}
