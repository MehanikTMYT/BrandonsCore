package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 23/12/19.
 */

@net.neoforged.fml.common.EventBusSubscriber (modid = MODID)
public class BCContent {

    //region Container Types.
    public static MenuType<ContainerPlayerAccess> containerPlayerAccess;
    //endregion

    @SubscribeEvent
    public static void onRegisterContainers(RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.MENU, e -> {
            containerPlayerAccess = IMenuTypeExtension.create(ContainerPlayerAccess::new);
            e.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(MODID, "player_access"), containerPlayerAccess);
        });
    }
}
