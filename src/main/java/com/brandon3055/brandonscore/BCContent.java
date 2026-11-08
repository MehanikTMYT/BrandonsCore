package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IForgeMenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.ObjectHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 23/12/19.
 */

@Mod.EventBusSubscriber (modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCContent {

    //region Container Types.
    @ObjectHolder (registryName = "menu", value = MODID + ":player_access") //Hmm.... I really dont like DeferredRegister but.....
    public static MenuType<ContainerPlayerAccess> containerPlayerAccess;
    //endregion

    @SubscribeEvent
    public static void onRegisterContainers(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.MENU_TYPES, e -> e.register("player_access", IForgeMenuType.create(ContainerPlayerAccess::new)));
    }
}
