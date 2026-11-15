package com.brandon3055.brandonscore.client.shader;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;

public record EnergyBarState(int x0, int y0, int x1, int y1, float charge, float time,
        Matrix3x2f pose, ScreenRectangle scissorArea) implements PictureInPictureRenderState {
    public float scale() { return 1; }
    public ScreenRectangle bounds() {
        var bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea == null ? bounds : scissorArea.intersection(bounds);
    }
}
