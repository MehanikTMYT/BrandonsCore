package com.brandon3055.brandonscore.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
 * The format is similar to the minecraft give command but with commas instead of spaces. <br>
 * There is also optional support for forge capabilities. <br>
 * This is primarily used in Project Intelligence and modular GUI. <br><br>
 * <p>
 * Valid Formats:                                               <br>
 * mod_domain:path                                              <br>
 * mod_domain:path,[count]                                      <br>
 * mod_domain:path{NBT}                                         <br>
 * mod_domain:path{NBT},[count]                                 <br>
 * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
 * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
 * <p>
 * Created by brandon3055 on 23/01/2022
 */
public class StringyStacks {
    public static final Logger LOGGER = LogManager.getLogger("StackConverter");

    /**
     * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
     * The format is similar to the minecraft give command but with commas instead of spaces. <br>
     * There is also optional support for forge capabilities. <br>
     * This is primarily used in Project Intelligence and modular GUI. <br><br>
     * <p>
     * Valid Formats:                                               <br>
     * mod_domain:path                                              <br>
     * mod_domain:path,[count]                                      <br>
     * mod_domain:path{NBT}                                         <br>
     * mod_domain:path{NBT},[count]                                 <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
     * <p>
     *
     * @param stackString The stack string
     * @return An item stack. Will default to empty stack if stack string is invalid.
     */
    public static ItemStack fromString(net.minecraft.core.HolderLookup.Provider registries, final String stackString) {
        return fromString(registries, stackString, ItemStack.EMPTY);
    }

    /**
     * This class contains methods for converting {@link ItemStack} to and from a somewhat user-friendly string. <br>
     * The format is similar to the minecraft give command but with commas instead of spaces. <br>
     * There is also optional support for forge capabilities. <br>
     * This is primarily used in Project Intelligence and modular GUI. <br><br>
     * <p>
     * Valid Formats:                                               <br>
     * mod_domain:path                                              <br>
     * mod_domain:path,[count]                                      <br>
     * mod_domain:path{NBT}                                         <br>
     * mod_domain:path{NBT},[count]                                 <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}}             <br>
     * mod_domain:path{tag:{NBT},ForgeCaps:{ForgeCaps}},[count]     <br>
     * <p>
     *
     * @param stackString           The stack string
     * @param defaultIfInputInvalid The default stack to return iin the event the stack string is invalid. (This can be null)
     * @return An item stack. Will default to defaultIfInputInvalid stack if stack string is invalid.
     */
    public static ItemStack fromString(net.minecraft.core.HolderLookup.Provider registries, final String stackString, @Nullable ItemStack defaultIfInputInvalid) {
        if (stackString.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!stackString.contains(":")) {
            LOGGER.warn("StackReference: Was given an invalid stack string. String did not contain \":\" - " + stackString);
            return defaultIfInputInvalid;
        }

        //Check for old legacy format where NBT is placed after count and (now invalid) meta data
        if (stackString.contains("{") && stackString.contains(",") && stackString.indexOf(",") < stackString.indexOf("{")) {
            try {
                return legacyFromString(stackString, defaultIfInputInvalid);
            }
            catch (Throwable e) {
                LOGGER.warn("Failed to parse legacy stack string - " + stackString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        String stackStr = stackString;
        String itemID;
        if (stackStr.contains("{")) {
            itemID = stackStr.substring(0, stackStr.indexOf("{"));
        } else if (stackStr.contains(",")) {
            itemID = stackStr.substring(0, stackStr.indexOf(","));
        } else {
            itemID = stackStr;
        }
        stackStr = stackString.substring(itemID.length());

        CompoundTag tagNBT = null;
        if (stackStr.startsWith("{")) {
            if (!stackString.contains("}") || stackStr.lastIndexOf("}") < stackStr.indexOf("{")) {
                LOGGER.warn("Detected invalid NBT definition on stack string - " + stackString);
                return defaultIfInputInvalid;
            } else {
                String tag = stackStr.substring(0, stackStr.lastIndexOf("}") + 1);
                try {
                    tagNBT = TagParser.parseCompoundFully(tag);
                }
                catch (Throwable e) {
                    LOGGER.warn("Failed to parse stack nbt from string - " + tag + ", on stack string - " + stackString + ". error: " + e.getMessage());
                    return defaultIfInputInvalid;
                }
                stackStr = stackStr.substring(tag.length());
            }
        }

        int count = 1;
        if (stackStr.startsWith(",") && stackString.length() > 1) {
            try {
                count = Integer.parseInt(stackStr.substring(1));
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack size from string - " + stackStr + ", on stack string - " + stackString + ". error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        try {
            CompoundTag stackNBT = new CompoundTag();
            stackNBT.putString("id", itemID);
            stackNBT.putInt("count", count);
            if (tagNBT != null && !tagNBT.isEmpty()) {
                if (tagNBT.get("components") instanceof CompoundTag components) {
                    stackNBT.put("components", components);
                } else {
                    // Legacy arbitrary NBT remains custom data. Historical ForgeCaps
                    // have no automatic mapping to a modern capability/component.
                    CompoundTag components = new CompoundTag();
                    components.put("minecraft:custom_data", tagNBT);
                    stackNBT.put("components", components);
                }
            }
            return ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stackNBT).getOrThrow();
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to decode stack string {}: {}", stackString, ex.getMessage());
            return defaultIfInputInvalid;
        }
    }

    private static ItemStack legacyFromString(String string, @Nullable ItemStack defaultIfInputInvalid) {
        String workString = string;
        String splitter = ",";

        String stackString;
        String countString = "";
        String metaString = "";
        String nbt = "";

        //Read Item Registry Name
        if (!workString.contains(splitter)) {
            stackString = workString;
            workString = "";
        } else {
            stackString = workString.substring(0, workString.indexOf(splitter));
            workString = workString.substring(workString.indexOf(splitter) + splitter.length());
        }

        //Read Stack Size
        if (!workString.isEmpty()) {
            if (!workString.contains(splitter)) {
                countString = workString;
                workString = "";
            } else {
                countString = workString.substring(0, workString.indexOf(splitter));
                workString = workString.substring(workString.indexOf(splitter) + splitter.length());
            }
        }
        //Read Stack Meta
        if (!workString.isEmpty()) {
            if (!workString.contains(splitter)) {
                metaString = workString;
                workString = "";
            } else {
                metaString = workString.substring(0, workString.indexOf(splitter));
                workString = workString.substring(workString.indexOf(splitter) + splitter.length());
            }
        }
        //Read Stack NBT
        if (!workString.isEmpty()) {
            nbt = workString;
        }

        int count = 1;
        int meta = 0;
        CompoundTag compound = null;

        if (!countString.isEmpty()) {
            try {
                count = Integer.parseInt(countString);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack size from string - " + countString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }
        if (!metaString.isEmpty()) {
            try {
                meta = Integer.parseInt(metaString);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack meta from string - " + metaString + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }
        if (!nbt.isEmpty()) {
            try {
                compound = TagParser.parseCompoundFully(nbt);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse stack nbt from string - " + nbt + " error: " + e.getMessage());
                return defaultIfInputInvalid;
            }
        }

        Identifier registryName = Identifier.parse(stackString);
        Item item = BuiltInRegistries.ITEM.getValue(registryName);
        Block block = BuiltInRegistries.BLOCK.getValue(registryName);
        if (item == null && block == Blocks.AIR) {
            return defaultIfInputInvalid;
        } else {
            ItemStack itemStack;
            if (item != null) {
                itemStack = new ItemStack(item, count);
            } else {
                itemStack = new ItemStack(block, count);
            }
            itemStack.setDamageValue(meta);

            if (compound != null) {
                itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(compound));
            }
            return itemStack;
        }
    }

    public static String toString(net.minecraft.core.HolderLookup.Provider registries, ItemStack stack, boolean withNBT, boolean withCount, boolean withForgeCaps) {
        if (stack.isEmpty()) {
            return "";
        }

        String stackString = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        if (withNBT || withForgeCaps) {
            CompoundTag stackTag = (CompoundTag) ItemStack.CODEC.encodeStart(
                    registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow();
            CompoundTag components = stackTag.getCompoundOrEmpty("components").copy();
            // Capability persistence is component-based in 26; there is no
            // separate ForgeCaps blob to select independently.
            if (!components.isEmpty()) {
                CompoundTag data = new CompoundTag();
                data.put("components", components);
                stackString += data;
            }
        }

        if (withCount && stack.getCount() > 1) {
            stackString += "," + stack.getCount();
        }

        return stackString;
    }

    /**
     * Includes everything except capabilities.
     * */
    public static String toStringNoCaps(net.minecraft.core.HolderLookup.Provider registries, ItemStack stack) {
        return toString(registries, stack, true, true, false);
    }

    /**
     * Default toString Method includes all item data including capabilities if they are present.
     */
    public static String toString(net.minecraft.core.HolderLookup.Provider registries, ItemStack stack) {
        return toString(registries, stack, true, true, true);
    }

    @Deprecated
    public static ItemStack legacyStackConverter(String itemString, int count, int damage, @Nullable CompoundTag nbt) {
        try {
            Identifier itemID = Identifier.parse(itemString);
            Item item = BuiltInRegistries.ITEM.getValue(itemID);
            Block block = BuiltInRegistries.BLOCK.getValue(itemID);
            if (item == null && block == Blocks.AIR) {
                return ItemStack.EMPTY;
            } else {
                ItemStack itemStack;
                if (item != null) {
                    itemStack = new ItemStack(item, count);
                } else {
                    itemStack = new ItemStack(block, count);
                }
                itemStack.setDamageValue(damage);

                if (nbt != null) {
                    itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt.copy()));
                }
                return itemStack;
            }
        }
        catch (Throwable e) {
            return ItemStack.EMPTY;
        }
    }

}
