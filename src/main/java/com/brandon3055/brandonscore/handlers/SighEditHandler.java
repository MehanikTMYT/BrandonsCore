package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.BrandonsCore;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;

/**
 * Created by brandon3055 on 24/01/2023
 */
public class SighEditHandler {

    private static final CrashLock LOCK = new CrashLock("Already Initialized");
    private static final GameRule<Boolean> ALLOW_SIGN_EDIT = new GameRule<>(GameRuleCategory.MISC,
            net.minecraft.world.level.gamerules.GameRuleType.BOOL,
            com.mojang.brigadier.arguments.BoolArgumentType.bool(),
            net.minecraft.world.level.gamerules.GameRuleTypeVisitor::visitBoolean,
            com.mojang.serialization.Codec.BOOL, value -> value ? 1 : 0, false,
            net.minecraft.world.flag.FeatureFlagSet.of());

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        LOCK.lock();
        modBus.addListener((net.neoforged.neoforge.registries.RegisterEvent event) ->
                event.register(net.minecraft.core.registries.Registries.GAME_RULE,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(BrandonsCore.MODID, "allow_sign_editing"),
                        () -> ALLOW_SIGN_EDIT));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(EventPriority.LOW, SighEditHandler::onBlockInteract);
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.isCanceled()) {
            return;
        }

        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || !player.getAbilities().mayBuild || !((net.minecraft.server.level.ServerLevel) level).getGameRules().get(ALLOW_SIGN_EDIT) || !player.getItemInHand(event.getHand()).isEmpty()) {
            return;
        }

        BlockEntity entity = level.getBlockEntity(event.getHitVec().getBlockPos());
        if (entity instanceof SignBlockEntity signEntity) {
//            signEntity.setEditable(true);
            player.openTextEdit(signEntity, true); //TODO Side
            event.setCanceled(true);
        }
    }
}
