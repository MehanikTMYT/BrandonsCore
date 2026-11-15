/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular;

import com.brandon3055.brandonscore.client.gui.modular.lib.GuiProvider;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A simple ModularGui screen implementation.
 * This is simply a wrapper for a {@link ModularGui} that takes a {@link GuiProvider}
 * This should be suitable for most basic gui screens.
 * <p>
 * Created by brandon3055 on 19/08/2023
 */
public class ModularGuiScreen extends Screen {

    protected final ModularGui modularGui;

    public ModularGuiScreen(GuiProvider provider) {
        super(Component.empty());
        this.modularGui = new ModularGui(provider);
        this.modularGui.setScreen(this);
    }

    public ModularGuiScreen(GuiProvider builder, Screen parentScreen) {
        super(Component.empty());
        this.modularGui = new ModularGui(builder, parentScreen);
        this.modularGui.setScreen(this);
    }

    public ModularGui getModularGui() {
        return modularGui;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return modularGui.getGuiTitle();
    }

    @Override
    public boolean isPauseScreen() {
        return modularGui.isPauseScreen();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return modularGui.closeOnEscape();
    }

    @Override
    protected void init() {
        modularGui.onScreenInit(minecraft, font, width, height);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        modularGui.onScreenInit(minecraft, font, width, height);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (modularGui.renderBackground()) {
            extractBackground(graphics, mouseX, mouseY, partialTicks);
        }
        GuiRender render = GuiRender.convert(graphics);
        modularGui.render(render, partialTicks);
        //Ensure we render overlay on top of things like JEI
        render.pose().pushPose();
        render.pose().translate(0, 0, 400);
        modularGui.renderOverlay(render, partialTicks);
        render.pose().popPose();
    }

    @Override
    public void tick() {
        modularGui.tick();
    }

    @Override
    public void removed() {
        modularGui.onGuiClose();
    }

    //=== Input Pass-though ===//

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        modularGui.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        return modularGui.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
        return modularGui.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return modularGui.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key(), scancode = event.scancode(), modifiers = event.modifiers();
        return modularGui.keyPressed(key, scancode, modifiers) || super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        int key = event.key(), scancode = event.scancode(), modifiers = event.modifiers();
        return modularGui.keyReleased(key, scancode, modifiers) || super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean handled = false;
        for (char character : event.codepointAsString().toCharArray()) {
            handled |= modularGui.charTyped(character, 0);
        }
        return handled || super.charTyped(event);
    }
}
