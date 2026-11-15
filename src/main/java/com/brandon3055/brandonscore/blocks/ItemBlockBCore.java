package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.IBCoreBlock;
import com.brandon3055.brandonscore.lib.ITilePlaceListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends BlockItem {

    public ItemBlockBCore(Block block, Item.Properties builder) {
        super(block, builder);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);

        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (placed && tile instanceof ITilePlaceListener) {
            ((ITilePlaceListener) tile).onTilePlaced(context, state);
        }

        return placed;
    }

    @Deprecated
    public CompoundTag getShareTag(ItemStack stack) {
        if (getBlock() instanceof IBCoreBlock && ((IBCoreBlock) getBlock()).overrideShareTag()) {
            return ((IBCoreBlock) getBlock()).getNBTShareTag(stack);
        }
        return com.brandon3055.brandonscore.utils.ItemNBTHelper.getCompound(stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (getBlock() instanceof BlockBCore block) {
            java.util.List<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>();
            block.appendHoverText(stack, lines);
            lines.forEach(tooltip);
        }
    }
}
