package com.rainworldmod.mechanics.rain;

import com.rainworldmod.mechanics.Easing;
import com.rainworldmod.mechanics.cycle.CycleTicker;
import com.rainworldmod.mechanics.cycle.CycleTimer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class RainTicker implements CycleTicker {
    private final long maxRainTime = -20*30;
    private final long deathRainTime = maxRainTime - 20*30;

    public double rainFactor;

    @Override
    public void onCycleTick(CycleTimer cycleTimer, World world) {
        if (world.isClient)
            return;

        if (cycleTimer.cycleTimeLeft >= 0)
            return;

        rainFactor = 1 - ((double) (maxRainTime - cycleTimer.cycleTimeLeft) / maxRainTime);
        rainFactor = Easing.easeOutBounce(rainFactor);

        ServerWorld serverWorld = (ServerWorld)world;

        if (rainFactor > 0.8)
            doThunder(serverWorld);
        else if (rainFactor > 0.1)
            doRain(serverWorld);
        else
            clearWeather(serverWorld);
    }

    private void doThunder(ServerWorld world)
    {
        world.setWeather(0, 10, true, true);
    }

    private void doRain(ServerWorld world)
    {
        world.setWeather(0, 10, true, false);
    }

    private void clearWeather(ServerWorld world)
    {
        world.setWeather(99999, 0, false, false);
    }
}
