package com.rainworldmod.mixin;

import com.rainworldmod.mechanics.CycleTimer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class TickTime extends World {
    protected TickTime(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Final
    @Shadow
    private ServerWorldProperties worldProperties;

    @Shadow public abstract ServerWorld toServerWorld();

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    protected void tickTime(ServerWorld instance, long timeOfDay)
    {
        CycleTimer worldTimer = CycleTimer.getCycleTimer(this.getRegistryKey());

        if (worldTimer == null)
            return;


        long currentTimeOfDay = (long) (worldTimer.getTimePercentage() * 12000);
        this.worldProperties.setTimeOfDay(currentTimeOfDay);
    }

    @Inject(method = "setTimeOfDay(J)V", at = @At("HEAD"))
    protected void setTimeOfDay(long timeOfDay, CallbackInfo info)
    {
        CycleTimer worldTimer = CycleTimer.getCycleTimer(this.getRegistryKey());

        if (worldTimer == null)
            return;

        worldTimer.cycleTimeLeft = (long) (worldTimer.cycleLength * (1 - (((float)timeOfDay / 12000) % 2)));
    }
}
