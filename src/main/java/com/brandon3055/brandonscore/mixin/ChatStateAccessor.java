package com.brandon3055.brandonscore.mixin;

import java.util.List;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Immediate removal and reflow for BC indexed chat messages. */
@Mixin(ChatComponent.class)
public interface ChatStateAccessor {
    @Accessor("allMessages") List<GuiMessage> bc$getMessages();
    @Invoker("refreshTrimmedMessages") void bc$refreshMessages();
}
