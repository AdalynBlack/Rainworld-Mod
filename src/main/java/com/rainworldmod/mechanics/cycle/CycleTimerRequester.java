package com.rainworldmod.mechanics.cycle;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class CycleTimerRequester {
    abstract void requestCycleTimer(RegistryKey<World> world);
}
