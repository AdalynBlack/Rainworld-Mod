package com.rainworldmod.mechanics.cycle;

import net.minecraft.world.World;

public interface CycleTicker {
    void onCycleTick(CycleTimer cycleTimer, World world);
    void startOfCycle(CycleTimer cycleTimer, World world);
}
