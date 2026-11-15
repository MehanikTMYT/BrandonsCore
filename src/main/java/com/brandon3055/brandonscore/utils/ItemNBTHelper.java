package com.brandon3055.brandonscore.utils;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.UUID;
import java.util.function.Consumer;

public final class ItemNBTHelper {
    private ItemNBTHelper() {}

    /** Returns a snapshot. Use updateCompound to persist changes to the stack. */
    public static CompoundTag getCompound(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void updateCompound(ItemStack stack, Consumer<CompoundTag> update) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, update);
    }

    public static boolean verifyExistance(ItemStack stack, String tag) {
        return getCompound(stack).contains(tag);
    }

    public static ItemStack setByte(ItemStack stack, String tag, byte b) {
        updateCompound(stack, data -> data.putByte(tag, b));
        return stack;
    }

    public static byte getByte(ItemStack stack, String tag, int defaultExpected) {
        return getCompound(stack).getByteOr(tag, (byte) defaultExpected);
    }

    public static ItemStack setBoolean(ItemStack stack, String tag, boolean b) {
        updateCompound(stack, data -> data.putBoolean(tag, b));
        return stack;
    }

    public static boolean getBoolean(ItemStack stack, String tag, boolean defaultExpected) {
        return getCompound(stack).getBooleanOr(tag, defaultExpected);
    }

    public static ItemStack setShort(ItemStack stack, String tag, short s) {
        updateCompound(stack, data -> data.putShort(tag, s));
        return stack;
    }

    public static short getShort(ItemStack stack, String tag, short defaultExpected) {
        return getCompound(stack).getShortOr(tag, defaultExpected);
    }

    public static ItemStack setInteger(ItemStack stack, String tag, int i) {
        updateCompound(stack, data -> data.putInt(tag, i));
        return stack;
    }

    public static int getInteger(ItemStack stack, String tag, int defaultExpected) {
        return getCompound(stack).getIntOr(tag, defaultExpected);
    }

    public static ItemStack setLong(ItemStack stack, String tag, long i) {
        updateCompound(stack, data -> data.putLong(tag, i));
        return stack;
    }

    public static long getLong(ItemStack stack, String tag, long defaultExpected) {
        return getCompound(stack).getLongOr(tag, defaultExpected);
    }

    public static ItemStack setFloat(ItemStack stack, String tag, float f) {
        updateCompound(stack, data -> data.putFloat(tag, f));
        return stack;
    }

    public static float getFloat(ItemStack stack, String tag, float defaultExpected) {
        return getCompound(stack).getFloatOr(tag, defaultExpected);
    }

    public static ItemStack setDouble(ItemStack stack, String tag, double d) {
        updateCompound(stack, data -> data.putDouble(tag, d));
        return stack;
    }

    public static double getDouble(ItemStack stack, String tag, double defaultExpected) {
        return getCompound(stack).getDoubleOr(tag, defaultExpected);
    }

    public static ItemStack setString(ItemStack stack, String tag, String s) {
        updateCompound(stack, data -> data.putString(tag, s));
        return stack;
    }

    public static String getString(ItemStack stack, String tag, String defaultExpected) {
        return getCompound(stack).getStringOr(tag, defaultExpected);
    }

    public static ItemStack setUUID(ItemStack stack, String tag, UUID uuid) {
        updateCompound(stack, data -> data.store(tag, UUIDUtil.CODEC, uuid));
        return stack;
    }

    public static UUID getUUID(ItemStack stack, String tag, UUID defaultExpected) {
        return getCompound(stack).read(tag, UUIDUtil.CODEC).orElse(defaultExpected);
    }
}
