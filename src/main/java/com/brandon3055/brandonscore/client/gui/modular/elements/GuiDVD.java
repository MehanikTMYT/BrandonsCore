/* Derived from CodeChickenLib, cbc26e1 parent. LGPL-2.1-or-later.
 * Copyright (C) 2013 Chicken-Bones; modular GUI by brandon3055.
 * Relocated for the BrandonsCore Minecraft 26 port; see LICENSE-CCL. */
package com.brandon3055.brandonscore.client.gui.modular.elements;

import com.brandon3055.brandonscore.client.gui.modular.lib.ContentElement;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modular.lib.GuiRender;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.Constraint;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.GuiParent;
import com.brandon3055.brandonscore.client.gui.modular.lib.geometry.Rectangle;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.Random;
import java.util.function.Consumer;

import static com.brandon3055.brandonscore.client.gui.modular.lib.geometry.GeoParam.*;

/**
 * Just for fun!
 * Created by brandon3055 on 10/09/2023
 */
public class GuiDVD extends GuiElement<GuiDVD> implements ContentElement<GuiElement<?>> {
    private static final Random randy = new Random();

    private final GuiElement<?> movingElement;
    private double xOffset = 0;
    private double yOffset = 0;
    private Vector2d velocity = null;
    private int bounce = 0;
    private Consumer<Integer> onBounce = bounce -> {
    };

    public GuiDVD(@NotNull GuiParent<?> parent) {
        super(parent);
        this.movingElement = new GuiElement<>(this)
                .constrain(TOP, Constraint.relative(get(TOP), () -> yOffset))
                .constrain(LEFT, Constraint.relative(get(LEFT), () -> xOffset))
                .constrain(WIDTH, Constraint.match(get(WIDTH)))
                .constrain(HEIGHT, Constraint.match(get(HEIGHT)));
    }

    @Override
    public GuiElement<?> getContentElement() {
        return movingElement;
    }

    public void start() {
        if (velocity == null) {
            velocity = new Vector2d(randy.nextBoolean() ? 1 : -1, randy.nextBoolean() ? 1 : -1);
            velocity.normalize();
        } else {
            velocity = null;
            xOffset = yOffset = 0;
            bounce = 0;
        }
    }

    public void onBounce(Consumer<Integer> onBounce) {
        this.onBounce = onBounce;
    }

    @Override
    public void tick(double mouseX, double mouseY) {
        super.tick(mouseX, mouseY);
    }

    @Override
    public void render(GuiRender render, double mouseX, double mouseY, float partialTicks) {
        super.render(render, mouseX, mouseY, partialTicks);
        if (velocity == null) return;

        double speed = 5 * partialTicks;
        xOffset += velocity.x * speed;
        yOffset += velocity.y * speed;
        Rectangle rect = movingElement.getRectangle();

        int bounces = 0;
        if ((velocity.y < 0 && rect.y() < 0) || (velocity.y > 0 && rect.yMax() > scaledScreenHeight())) {
            velocity.y *= -1;
            onBounce.accept(bounce++);
            bounces++;
        }

        if ((velocity.x < 0 && rect.x() < 0) || (velocity.x > 0 && rect.xMax() > scaledScreenWidth())) {
            velocity.x *= -1;
            onBounce.accept(bounce++);
            bounces++;
        }

        if (bounce > 0) {
            velocity.y += -0.05 + (randy.nextGaussian() * 0.1);
            velocity.x += -0.05 + (randy.nextGaussian() * 0.1);
            velocity.x = velocity.x > 0 ? MathHelper.clip(velocity.x, 0.4, 0.6) : MathHelper.clip(velocity.x, -0.4, -0.6);
            velocity.y = velocity.y > 0 ? MathHelper.clip(velocity.y, 0.4, 0.6) : MathHelper.clip(velocity.y, -0.4, -0.6);
            velocity.normalize();
        }
    }
}
