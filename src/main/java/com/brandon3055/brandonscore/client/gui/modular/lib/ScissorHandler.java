/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.lib;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;

/**
 * Created by brandon3055 on 29/06/2023
 */
public class ScissorHandler {
    public static final Logger LOGGER = LogManager.getLogger();
    private final net.minecraft.client.gui.GuiGraphicsExtractor extractor;

    public ScissorHandler(net.minecraft.client.gui.GuiGraphicsExtractor extractor) {
        this.extractor = java.util.Objects.requireNonNull(extractor);
    }

    private final Deque<ScissorState> stack = Queues.newArrayDeque();

    public void pushGuiScissor(double x, double y, double width, double height) {
        Window window = Minecraft.getInstance().getWindow();
        int windowHeight = window.getHeight();
        double scale = window.getGuiScale();
        double scX = x * scale;
        double scY = (double) windowHeight - (y + height) * scale;
        double scW = Math.max(width * scale, 0);
        double scH = Math.max(height * scale, 0);
        pushScissor((int) scX, (int) scY, (int) scW, (int) scH);
    }

    public void pushScissor(int x, int y, int width, int height) {
        int xMax = x + width;
        int yMax = y + height;
        ScissorState state = ScissorState.createState(x, y, xMax, yMax, stack.peekLast());
        stack.addLast(state);
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        extractor.enableScissor((int) Math.floor(state.x / scale), (int) Math.floor((window.getHeight() - state.yMax) / scale),
                (int) Math.ceil(state.xMax / scale), (int) Math.ceil((window.getHeight() - state.y) / scale));
    }

    public void popScissor() {
        if (stack.isEmpty()) {
            LOGGER.error("Scissor stack underflow");
            return;
        }
        stack.removeLast();
        extractor.disableScissor();
    }

    private record ScissorState(int x, int y, int xMax, int yMax) {

        private static ScissorState createState(int newX, int newY, int newXMax, int newYMax, ScissorState prevState) {
            if (prevState != null) {
                int x = Math.max(prevState.x, newX);
                int y = Math.max(prevState.y, newY);
                int xMax = Math.min(prevState.xMax, newXMax);
                int yMax = Math.min(prevState.yMax, newYMax);
                Minecraft mc = Minecraft.getInstance();
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (xMax > mc.getWindow().getScreenWidth()) xMax = mc.getWindow().getScreenWidth();
                if (yMax > mc.getWindow().getScreenHeight()) yMax = mc.getWindow().getScreenHeight();
                if (xMax < x) xMax = x;
                if (yMax < y) yMax = y;
                return new ScissorState(x, y, xMax, yMax);
            } else {
                return new ScissorState(newX, newY, newXMax, newYMax);
            }
        }
    }
}
