package com.brandon3055.brandonscore.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Client layout updates must also move vanilla's input hitboxes. */
@Mixin(Slot.class)
public interface SlotPositionAccessor {
    @Mutable @Accessor("x") void brandonscore$setX(int x);
    @Mutable @Accessor("y") void brandonscore$setY(int y);
}
