package com.rainworldmod.mechanics.rain;

import com.rainworldmod.RainworldMod;
import com.rainworldmod.mechanics.Easing;
import com.rainworldmod.mechanics.cycle.CycleTicker;
import com.rainworldmod.mechanics.cycle.CycleTimer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RainTicker implements CycleTicker {
    public static final long MAX_RAIN_TIME = -20*30;
    public static final long DEATH_RAIN_TIME = MAX_RAIN_TIME - 20*30;

    private static final TagKey<Biome> SHELTER = TagKey.of(RegistryKeys.BIOME, new Identifier(RainworldMod.MOD_ID, "shelter"));
    private static final TagKey<Biome> NO_RAIN = TagKey.of(RegistryKeys.BIOME, new Identifier(RainworldMod.MOD_ID, "no_rain"));

    private static final Set<TagKey<Biome>> RAIN_SURVIVAL_TAGS = new HashSet<>(List.of(
            SHELTER,
            NO_RAIN
    ));

    public double rainFactor;

    @Override
    public void onCycleTick(CycleTimer cycleTimer, World world) {
        if (world.isClient)
            return;

        if (cycleTimer.cycleTimeLeft >= 0)
            return;

        rainFactor = 1 - ((double) (MAX_RAIN_TIME - cycleTimer.cycleTimeLeft) / MAX_RAIN_TIME);
        rainFactor = Easing.easeOutBounce(rainFactor);

        ServerWorld serverWorld = (ServerWorld)world;

        if (rainFactor > 0.8)
            doThunder(serverWorld);
        else if (rainFactor > 0.1)
            doRain(serverWorld);
        else
            clearWeather(serverWorld);

        if (cycleTimer.cycleTimeLeft > DEATH_RAIN_TIME)
            return;

        world.getPlayers().forEach((player) -> {
            if (!canDieToRain(player))
                return;

            player.damage(player.getDamageSources().drown(), Float.MAX_VALUE);
        });
    }

    @Override
    public void reset(CycleTimer cycleTimer, World world) {
        rainFactor = 0;
    }

    public static boolean canDieToRain(PlayerEntity player) {
        return player.getWorld().getBiome(player.getBlockPos())
                .streamTags().noneMatch(RAIN_SURVIVAL_TAGS::contains) && !player.canModifyBlocks();
    }

    public static boolean isSheltered(PlayerEntity player) {
        return player.getWorld().getBiome(player.getBlockPos())
                .streamTags().anyMatch((tag) -> tag == SHELTER);
    }

    private void doThunder(ServerWorld world) {
        world.setWeather(0, 10, true, true);
    }

    private void doRain(ServerWorld world) {
        world.setWeather(0, 10, true, false);
    }

    private void clearWeather(ServerWorld world) {
        world.setWeather(99999, 0, false, false);
    }
}
