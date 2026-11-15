package com.brandon3055.brandonscore.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 13/9/19.
 */
public class TileItemStackHandler extends ItemStackHandler {

    private final BlockEntity tile;
    private BiPredicate<Integer, ItemStack> stackValidator = null;
    private Runnable loadListener = null;
    private Consumer<Integer> contentsChangeListener = null;
    private Map<Integer, Predicate<ItemStack>> slotValidators = new HashMap<>();
    private ItemStack prevStack = ItemStack.EMPTY;
    private Supplier<Integer> perSlotLimit = null;
    private int slotLimit = 64;

    public TileItemStackHandler(BlockEntity tile) {
        this.tile = tile;
    }

    public TileItemStackHandler(BlockEntity tile, int size) {
        super(size);
        this.tile = tile;
    }

    public TileItemStackHandler(BlockEntity tile, NonNullList<ItemStack> stacks) {
        super(stacks);
        this.tile = tile;
    }

    public TileItemStackHandler setStackValidator(BiPredicate<Integer, ItemStack> stackValidator) {
        this.stackValidator = stackValidator;
        return this;
    }

    public TileItemStackHandler setStackValidator(Predicate<ItemStack> stackValidator) {
        this.stackValidator = (integer, stack) -> stackValidator.test(stack);
        return this;
    }

    public TileItemStackHandler setSlotValidator(int slot, Predicate<ItemStack> validator) {
        slotValidators.put(slot, validator);
        return this;
    }

    public TileItemStackHandler setLoadListener(Runnable loadListener) {
        this.loadListener = loadListener;
        return this;
    }

    public TileItemStackHandler setContentsChangeListener(Consumer<Integer> contentsChangeListener) {
        this.contentsChangeListener = contentsChangeListener;
        return this;
    }

    public TileItemStackHandler setPerSlotLimit(Supplier<Integer> perSlotLimit) {
        this.perSlotLimit = perSlotLimit;
        return this;
    }

    public TileItemStackHandler setSlotLimit(int slotLimit) {
        this.slotLimit = slotLimit;
        return this;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        prevStack = getStackInSlot(slot);
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        prevStack = getStackInSlot(slot);
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slotValidators.containsKey(slot)) {
            return slotValidators.get(slot).test(stack);
        }
        return stackValidator == null || stackValidator.test(slot, stack);
    }

    // The registered handler size is authoritative, never the serialized data.
    @Override
    public void serialize(net.minecraft.world.level.storage.ValueOutput output) {
        var items = output.childrenList("Items");
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i).isEmpty()) continue;
            var item = items.addChild();
            item.putInt("Slot", i);
            item.store(ItemStack.MAP_CODEC, stacks.get(i));
        }
    }

    @Override
    public void deserialize(net.minecraft.world.level.storage.ValueInput input) {
        // NonNullList.clear keeps its fixed size and restores EMPTY entries.
        stacks.clear();
        for (var item : input.childrenListOrEmpty("Items")) {
            int slot = item.getIntOr("Slot", -1);
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, item.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY));
            }
        }
        onLoad();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (contentsChangeListener != null) {
            contentsChangeListener.accept(slot);
        }
        tile.setChanged();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (loadListener != null) {
            loadListener.run();
        }
    }

    public ItemStack getListenerPrevStack() {
        return prevStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return perSlotLimit == null ? slotLimit : perSlotLimit.get();
    }
}

