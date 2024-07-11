package com.rainworldmod.mechanics;

import com.rainworldmod.RainworldMod;
import com.rainworldmod.networking.SyncCycleTimer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.HashMap;

public class CycleTimer extends PersistentState {
    public int cycleLength;
    public long cycleTimeLeft;
    public int minimumCycleTime;
    public int maximumCycleTime;

    public RegistryKey<World> world;

    private boolean firstTimeSetHappened = false;

    private static final HashMap<RegistryKey<World>, CycleTimer> CYCLE_TIMERS = new HashMap<>();

    public CycleTimer() {
        this(-1, -1, 20*60*9, 20*60*15);
    }

    public CycleTimer(int cycleLength, long cycleTimeLeft, int minimumCycleTime, int maximumCycleTime) {
        this.cycleLength = cycleLength;
        this.cycleTimeLeft = cycleTimeLeft;
        this.minimumCycleTime = minimumCycleTime;
        this.maximumCycleTime = maximumCycleTime;
    }

    public float getTimePercentage()
    {
        return 1 - ((float) cycleTimeLeft / cycleLength);
    }

    public static CycleTimer getCycleTimer(RegistryKey<World> world) {
        return CYCLE_TIMERS.get(world);
    }

    public void advanceRainTimer(long rwTimeDelta)
    {
        if (!firstTimeSetHappened) {
            firstTimeSetHappened = true;
            return;
        }

        this.cycleTimeLeft += rwTimeDelta;

        this.markDirty();
    }

    public float getTimeMultiplier()
    {
        return 12000f / this.cycleLength;
    }

    public long convertToRW(long mcTime)
    {
        return (long) (mcTime / getTimeMultiplier());
    }

    public long convertToMC(long rwTime)
    {
        return (long) (rwTime * getTimeMultiplier());
    }

    public void SelectNextRainTime(World world) {
        this.cycleLength = world.getRandom().nextBetween(minimumCycleTime, maximumCycleTime);
        this.cycleTimeLeft = this.cycleLength;

        this.markDirty();
    }

    private static void worldStartTick(World world) {
        CycleTimer worldTimer = CYCLE_TIMERS.get(world.getRegistryKey());
        if (worldTimer.getTimePercentage() > 1.5)
            return;
        if (!world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
            return;
        //if (!world.getTick().shouldTick())
        //    return;

        worldTimer.cycleTimeLeft--;

        worldTimer.markDirty();

        if (world.isClient)
            return;

        if (worldTimer.cycleTimeLeft < 0) {
            world.getLevelProperties().setRaining(true);
            ((ServerWorldProperties) (world.getLevelProperties())).setThundering(true);
        }
        else if (worldTimer.getTimePercentage() > 0.95) {
            world.getLevelProperties().setRaining(true);
            ((ServerWorldProperties) (world.getLevelProperties())).setThundering(false);
        }
        else {
            world.getLevelProperties().setRaining(false);
            ((ServerWorldProperties) (world.getLevelProperties())).setThundering(false);
        }
    }

    /*
    private static final Type<WorldTimer> TYPE = new Type<>(
            WorldTimer::new,
            WorldTimer::createFromNbt,
            null
    );
     */

    public static CycleTimerRequester cycleTimerRequester;

    private static void load(MinecraftServer minecraftServer, World world) {
        CycleTimer worldTimer = null;

        if (world.isClient) {
            worldTimer = new CycleTimer();

            if (cycleTimerRequester != null)
                cycleTimerRequester.requestCycleTimer(world.getRegistryKey());
        } else {
            ServerWorld serverWorld = (ServerWorld) world;

            PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
            worldTimer = persistentStateManager.getOrCreate(CycleTimer::createFromNbt, CycleTimer::new, RainworldMod.MOD_ID);

            if (worldTimer.cycleLength == -1)
                worldTimer.SelectNextRainTime(serverWorld);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeRegistryKey(world.getRegistryKey());
            buf.writeInt(worldTimer.cycleLength);
            buf.writeLong(worldTimer.cycleTimeLeft);

            for (ServerPlayerEntity player : PlayerLookup.world(serverWorld))
                ServerPlayNetworking.send(player, SyncCycleTimer.SYNC_CYCLE_TIMER_PACKET_ID, new SyncCycleTimer(world.getRegistryKey(), worldTimer.cycleLength, worldTimer.cycleTimeLeft).toBuf());
        }

        CYCLE_TIMERS.put(world.getRegistryKey(), worldTimer);
        worldTimer.world = world.getRegistryKey();
    }

    public static void initialize() {
        ServerWorldEvents.LOAD.register(CycleTimer::load);
        ServerWorldEvents.UNLOAD.register(CycleTimer::unload);
        ServerTickEvents.START_WORLD_TICK.register(CycleTimer::worldStartTick);
    }

    private static void unload(MinecraftServer minecraftServer, ServerWorld world) {
        CYCLE_TIMERS.get(world.getRegistryKey());
        CYCLE_TIMERS.remove(world.getRegistryKey());
    }

    public static CycleTimer createFromNbt(NbtCompound tag) {
        return new CycleTimer(
                tag.getInt("cycleLength"),
                tag.getLong("cycleTimeLeft"),
                tag.getInt("minimumCycleTime"),
                tag.getInt("maximumCycleTime"));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("cycleLength", cycleLength);
        nbt.putLong("cycleTimeLeft", cycleTimeLeft);
        nbt.putInt("minimumCycleTime", minimumCycleTime);
        nbt.putInt("maximumCycleTime", maximumCycleTime);
        return nbt;
    }
}
