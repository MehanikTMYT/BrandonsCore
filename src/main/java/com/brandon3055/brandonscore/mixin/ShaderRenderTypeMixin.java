package com.brandon3055.brandonscore.mixin;

import com.brandon3055.brandonscore.client.shader.ShaderDraw;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public abstract class ShaderRenderTypeMixin {
    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
    private void brandonscore$parameters(RenderPass pass, int baseVertex, int firstIndex, int count, int instances) {
        ShaderDraw.draw((RenderType)(Object)this, pass, baseVertex, firstIndex, count, instances);
    }
}
