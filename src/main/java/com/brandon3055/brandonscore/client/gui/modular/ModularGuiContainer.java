/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular;

import com.brandon3055.brandonscore.client.gui.modular.elements.GuiElement;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import com.brandon3055.brandonscore.client.gui.modular.lib.container.ContainerGuiProvider;
import com.brandon3055.brandonscore.client.gui.modular.lib.container.ContainerScreenAccess;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.GeoParam;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Container screen implementation for {@link ModularGui}.
 *
 * <p>
 * Created by brandon3055 on 08/09/2023
 */
public class ModularGuiContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerScreenAccess<T> {

    public final ModularGui modularGui;
    /**
     * Flag used to disable vanilla slot highlight rendering.
     * */
    private boolean renderingSlots = false;

    public ModularGuiContainer(T containerMenu, Inventory inventory, ContainerGuiProvider<T> provider) {
        super(containerMenu, inventory, Component.empty());
        provider.setMenuAccess(this);
        this.modularGui = new ModularGui(provider);
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
        GuiElement<?> root = modularGui.getRoot();
        topPos = (int) root.getValue(GeoParam.TOP);
        leftPos = (int) root.getValue(GeoParam.LEFT);


        if (modularGui.renderBackground()) {
            //Called here to ensure background renders before modularGui elements.
            super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        }
        GuiRender render = GuiRender.convert(graphics);
        modularGui.render(render, partialTicks);

        renderingSlots = !modularGui.vanillaSlotRendering();
        try {
            super.extractContents(graphics, mouseX, mouseY, partialTicks);
        } finally {
            renderingSlots = false;
        }
        hoveredSlot = findSlot(mouseX, mouseY);
        // Vanilla owns carried/snapback extraction in 26, including its animation state.
        super.extractCarriedItem(graphics, mouseX, mouseY);
        super.extractSnapbackItem(graphics);

        //Ensure we render overlay on top of things like JEI
        render.pose().pushPose();
        render.pose().translate(0, 0, 400);
        if (!(modularGui.doesFloatingItemDisableToolTips() && !menu.getCarried().isEmpty()) && !renderHoveredStackToolTip(render, mouseX, mouseY)) {
            modularGui.renderOverlay(render, partialTicks);
        }
        render.pose().popPose();
    }

    protected boolean renderHoveredStackToolTip(GuiRender guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            GuiElement<?> handler = modularGui.getSlotHandler(hoveredSlot);
            if (handler != null && (handler.blockMouseOver(handler, mouseX, mouseY) || !handler.isMouseOver())) {
                return false;
            }
            ItemStack itemStack = this.hoveredSlot.getItem();
            guiGraphics.toolTipWithImage(this.getTooltipFromContainerItem(itemStack), itemStack.getTooltipImage(), itemStack, mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void containerTick() {
        modularGui.tick();
    }

    @Override
    public void removed() {
        super.removed();
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

    //=== AbstractContainerMenu Overrides ===//

    @Override
    protected void extractSlot(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY) {
        if (modularGui.vanillaSlotRendering()) {
            super.extractSlot(guiGraphics, slot, mouseX, mouseY);
        } else {
            renderingSlots = true;
        }
    }

    //Modular gui friendly version of the slot render
    @Override
    public void renderSlot(GuiRender render, Slot slot) {
        if (modularGui.vanillaSlotRendering()) return;
        // Vanilla owns touchscreen splitting, drag placement, fake slots and decorations.
        render.withExtraction(() -> {
            var graphics = render.guiGraphicsWrapper();
            graphics.pose().pushMatrix();
            try {
                graphics.pose().translate(leftPos, topPos);
                super.extractSlot(graphics, slot, 0, 0);
            } finally {
                graphics.pose().popMatrix();
            }
        });
    }

    @Override
    protected boolean isHovering(int left, int top, int w, int h, double mouseX, double mouseY) {
        if (renderingSlots) return false;
        if (!super.isHovering(left, top, w, h, mouseX, mouseY)) return false;
        for (Slot slot : menu.slots) {
            if (slot.x == left && slot.y == top) {
                GuiElement<?> handler = modularGui.getSlotHandler(slot);
                if (handler != null && (!handler.isEnabled() || !handler.isMouseOver()
                        || handler.blockMouseOver(handler, mouseX, mouseY))) return false;
            }
        }
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        GuiElement<?> root = modularGui.getRoot();
        return !GuiRender.isInRect(root.xMin(), root.yMin(), root.xSize(), root.ySize(), mouseX, mouseY);
    }

    @Override //Disable vanilla title and inventory name rendering
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int i, int j) {
    }

    public void renderFloatingItem(GuiRender render, ItemStack itemStack, int x, int y, String string) {
        render.renderItem(itemStack, x, y);
        render.renderItemDecorations(itemStack, x, y, string);
    }

    @Nullable
    public Slot findSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) return slot;
        }
        return null;
    }

    @Override
    protected void slotClicked(Slot slot, int i, int j, ContainerInput clickType) {
        if (slot != null) {
            GuiElement<?> handler = modularGui.getSlotHandler(slot);
            if (handler != null && !handler.isEnabled()) return;
        }
        super.slotClicked(slot, i, j, clickType);
    }
}
