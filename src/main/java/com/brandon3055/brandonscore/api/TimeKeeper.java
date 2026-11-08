package com.brandon3055.brandonscore.api;

import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * Created by brandon3055 on 12/10/19.
 */
public class TimeKeeper {

    private static int serverTick = 0;
    private static int clientTick = 0;

    static {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new TimeKeeper());
    }

    @SubscribeEvent
    protected void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            clientTick++;
        }
    }

    @SubscribeEvent
    protected void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            serverTick++;
        }
    }

    public static int getServerTick() {
        return serverTick;
    }

    public static int getClientTick() {
        return clientTick;
    }

    public static int interval(int intervalTicks, int rollover) {
        int tick = EffectiveSide.get().isClient() ? getClientTick() : getServerTick();
        return (tick / intervalTicks) % rollover;
    }

}
