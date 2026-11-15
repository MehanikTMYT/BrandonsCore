package com.brandon3055.brandonscore.client;

import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nullable;
import java.util.ListIterator;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public Level getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public boolean isCTRLKeyDown() {
        return net.minecraft.client.Minecraft.getInstance().hasControlDown();
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public void sendIndexedMessage(Player player, Component message, MessageSignature signature) {
        Minecraft mc = Minecraft.getInstance();
        if (message == null) {
            deleteMessage(signature);
        } else {
            deleteMessage(signature);
            mc.gui.getChat().addPlayerMessage(message, signature, null);
        }
    }

    public void deleteMessage(MessageSignature signature) {
        Minecraft mc = Minecraft.getInstance();
        var chat = (com.brandon3055.brandonscore.mixin.ChatStateAccessor) mc.gui.getChat();
        if (chat.bc$getMessages().removeIf(entry -> signature.equals(entry.signature()))) {
            chat.bc$refreshMessages();
        }
    }

    @Override
    public void setClipboardString(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
