package com.brandon3055.brandonscore.worldentity;


/**
 * Created by brandon3055 on 15/12/20
 */
public interface ITickableWorldEntity {
    enum Phase { START, END }
    void tick();

    /**
     * Allows you to choose weather this entity ticks at the start or the end of the world tick. Default is start.
     * @return Phase.START or Phase.END
     */
    default Phase getPhase() {
        return Phase.START;
    }
}
