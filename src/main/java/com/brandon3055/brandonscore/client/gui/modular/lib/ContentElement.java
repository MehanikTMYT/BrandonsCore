/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.lib;


import com.brandon3055.brandonscore.client.gui.modular.elements.GuiElement;

/**
 * Implemented by elements that have a separate child element to which content should be added.
 * e.g. scroll element, manipulable element etc...
 * Created by brandon3055 on 13/11/2023
 */
public interface ContentElement<T extends GuiElement<?>> {

    T getContentElement();
}
