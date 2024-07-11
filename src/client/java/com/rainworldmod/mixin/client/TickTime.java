package com.rainworldmod.mixin.client;

import com.rainworldmod.mechanics.cycle.CycleTimer;
import com.rainworldmod.mechanics.sun.SunAnimator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class TickTime extends World {
    protected TickTime(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow @Final private ClientWorld.Properties clientWorldProperties;

    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setTimeOfDay(J)V"))
    protected void tickTime(ClientWorld instance, long timeOfDay)
    {
        CycleTimer cycleTimer = CycleTimer.getCycleTimer(this.getRegistryKey());

        if (cycleTimer == null)
            return;

        long currentTimeOfDay = SunAnimator.getSimulatedTimeOfDay(cycleTimer.getTimePercentage());
        this.clientWorldProperties.setTimeOfDay(currentTimeOfDay);
    }

    @Inject(method = "setTimeOfDay(J)V", at = @At("HEAD"))
    protected void setTimeOfDay(long timeOfDay, CallbackInfo info)
    {
        CycleTimer cycleTimer = CycleTimer.getCycleTimer(this.getRegistryKey());

        if (cycleTimer == null)
            return;

        long timeDelta = timeOfDay - this.getTimeOfDay();

        timeDelta = cycleTimer.convertToRW(timeDelta);
        cycleTimer.advanceCycleTimer(timeDelta);
    }
}
