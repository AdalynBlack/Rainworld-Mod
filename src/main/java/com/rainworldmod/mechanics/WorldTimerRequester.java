package com.rainworldmod.mechanics;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class WorldTimerRequester {
    abstract void requestWorldTimer(RegistryKey<World> world);
}
