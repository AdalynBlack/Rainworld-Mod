package com.rainworldmod.mechanics.sleep;

import com.rainworldmod.RainworldMod;
import com.rainworldmod.mechanics.cycle.CycleTicker;
import com.rainworldmod.mechanics.cycle.CycleTimer;
import com.rainworldmod.mechanics.rain.RainTicker;
import com.rainworldmod.mechanics.sun.SunAnimator;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class CycleSleep implements CycleTicker {

    private boolean sleepingThroughCycle = false;
    private boolean hasLockedShelters = false;
    private boolean hasShownKarma = false;

    private static final String HAS_NOT_LEFT_SHELTER = "HAS_NOT_LEFT_SHELTER";

    @Override
    public void onCycleTick(CycleTimer cycleTimer, World world) {
        if (cycleTimer.getTimePercentage() >= 0 && hasLockedShelters && !sleepingThroughCycle)
            unlockShelters(world);

        world.getPlayers().stream()
                .filter((player) -> player.getCommandTags().contains(HAS_NOT_LEFT_SHELTER))
                .filter((player) -> !RainTicker.isSheltered(player))
                .forEach((player) -> player.getCommandTags().remove(HAS_NOT_LEFT_SHELTER));

        if (cycleTimer.getTimePercentage() >= 0.2 && !sleepingThroughCycle)
            world.getPlayers()
                    .forEach((player) -> player.getCommandTags().remove(HAS_NOT_LEFT_SHELTER));

        if (sleepingThroughCycle || canAllPlayersSleep(world))
            sleepThroughCycle(cycleTimer, world);
    }

    public void sleepThroughCycle(CycleTimer cycleTimer, World world) {
        this.sleepingThroughCycle = true;

        if (!this.hasLockedShelters)
            lockShelters(world);

        world.getPlayers().stream()
                .filter(RainTicker::isSheltered)
                .forEach((player) -> player.addCommandTag(HAS_NOT_LEFT_SHELTER));

        if (cycleTimer.cycleTimeLeft > 0) {
            cycleTimer.cycleTimeLeft -= cycleTimer.cycleLength / (20 * 10);
            return;
        }

        if (!this.hasShownKarma)
            showKarma(world);

        if (cycleTimer.cycleTimeLeft == RainTicker.MAX_RAIN_TIME)
            world.getPlayers().forEach((player)->
                    player.sendMessage(Text.literal("Max rain has started!")));

        if (cycleTimer.cycleTimeLeft == RainTicker.DEATH_RAIN_TIME)
            world.getPlayers().forEach((player)->
                    player.sendMessage(Text.literal("Death rain has triggered!")));
        CreeperEntity
        if (cycleTimer.cycleTimeLeft > RainTicker.DEATH_RAIN_TIME - 20 * 10)
            return;

        if (cycleTimer.getTimePercentage() < SunAnimator.NIGHT_CYCLE_END)
            return;

        if (!isKarmaDone())
            return;

        cycleTimer.reset(world);
        cycleTimer.cycleTimeLeft -= (long) (SunAnimator.PRE_CYCLE_START * cycleTimer.cycleLength);
    }

    public void reset(CycleTimer cycleTimer, World world)
    {
        this.sleepingThroughCycle = false;
        this.hasShownKarma = false;
    }

    public void lockShelters(World world) {
        RainworldMod.LOGGER.info("Shelters Locked");
        world.getPlayers().forEach((player -> player.sendMessage(Text.literal("Shelters have been locked!"))));
        this.hasLockedShelters = true;
    }

    public void unlockShelters(World world) {
        RainworldMod.LOGGER.info("Shelters Unlocked");
        world.getPlayers().forEach((player -> player.sendMessage(Text.literal("Shelters have been unlocked!"))));
        this.hasLockedShelters = false;
    }

    public void showKarma(World world) {
        RainworldMod.LOGGER.info("Karma Shown");
        world.getPlayers().forEach((player -> player.sendMessage(Text.literal("Karma has been shown!"))));
        this.hasShownKarma = true;
    }

    public boolean isKarmaDone() { return true; }

    public boolean canAllPlayersSleep(World world)
    {
        List<? extends PlayerEntity> players = world.getPlayers();

        for (PlayerEntity player : players)
            if (doesPlayerCancelSleep(player))
                return false;

        return true;
    }

    public boolean doesPlayerCancelSleep(PlayerEntity player)
    {
        return RainTicker.canDieToRain(player) || player.getCommandTags().contains(HAS_NOT_LEFT_SHELTER);
    }
}
