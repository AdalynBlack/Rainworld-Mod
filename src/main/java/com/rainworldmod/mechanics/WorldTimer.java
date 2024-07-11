package com.rainworldmod.mechanics;

import com.rainworldmod.RainworldMod;
import com.rainworldmod.networking.SyncWorldTimer;
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

public class WorldTimer extends PersistentState {
    public int cycleLength;
    public long cycleTimeLeft;
    public int minimumCycleTime;
    public int maximumCycleTime;

    public RegistryKey<World> world;

    private boolean firstTimeSetHappened = false;

    private static final HashMap<RegistryKey<World>, WorldTimer> WORLD_TIMERS = new HashMap<>();

    public WorldTimer() {
        this(-1, -1, 20*60*9, 20*60*15);
    }

    public WorldTimer(int cycleLength, long cycleTimeLeft, int minimumCycleTime, int maximumCycleTime) {
        this.cycleLength = cycleLength;
        this.cycleTimeLeft = cycleTimeLeft;
        this.minimumCycleTime = minimumCycleTime;
        this.maximumCycleTime = maximumCycleTime;
    }

    public float getTimePercentage()
    {
        return 1 - ((float) cycleTimeLeft / cycleLength);
    }

    public static WorldTimer getWorldTimer(RegistryKey<World> world) {
        return WORLD_TIMERS.get(world);
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
        WorldTimer worldTimer = WORLD_TIMERS.get(world.getRegistryKey());
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

    public static WorldTimerRequester worldTimerRequester;

    private static void load(MinecraftServer minecraftServer, World world) {
        WorldTimer worldTimer = null;

        if (world.isClient) {
            worldTimer = new WorldTimer();

            if (worldTimerRequester != null)
                worldTimerRequester.requestWorldTimer(world.getRegistryKey());
        } else {
            ServerWorld serverWorld = (ServerWorld) world;

            PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
            worldTimer = persistentStateManager.getOrCreate(WorldTimer::createFromNbt, WorldTimer::new, RainworldMod.MOD_ID);

            if (worldTimer.cycleLength == -1)
                worldTimer.SelectNextRainTime(serverWorld);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeRegistryKey(world.getRegistryKey());
            buf.writeInt(worldTimer.cycleLength);
            buf.writeLong(worldTimer.cycleTimeLeft);

            for (ServerPlayerEntity player : PlayerLookup.world(serverWorld))
                ServerPlayNetworking.send(player, SyncWorldTimer.SYNC_WORLD_TIMER_PACKET_ID, new SyncWorldTimer(world.getRegistryKey(), worldTimer.cycleLength, worldTimer.cycleTimeLeft).toBuf());
        }

        WORLD_TIMERS.put(world.getRegistryKey(), worldTimer);
        worldTimer.world = world.getRegistryKey();
    }

    public static void initialize() {
        ServerWorldEvents.LOAD.register(WorldTimer::load);
        ServerWorldEvents.UNLOAD.register(WorldTimer::unload);
        ServerTickEvents.START_WORLD_TICK.register(WorldTimer::worldStartTick);
    }

    private static void unload(MinecraftServer minecraftServer, ServerWorld world) {
        WORLD_TIMERS.get(world.getRegistryKey());
        WORLD_TIMERS.remove(world.getRegistryKey());
    }

    public static WorldTimer createFromNbt(NbtCompound tag) {
        return new WorldTimer(
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
