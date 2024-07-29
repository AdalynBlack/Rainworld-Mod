package com.rainworldmod.mechanics.cycle;

import com.rainworldmod.RainworldMod;
import com.rainworldmod.mechanics.rain.RainTicker;
import com.rainworldmod.mechanics.sleep.CycleSleep;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class CycleTimer extends PersistentState {
    public int cycleLength;
    public long cycleTimeLeft;
    public int minimumCycleTime;
    public int maximumCycleTime;

    public RegistryKey<World> world;

    private static final List<Supplier<CycleTicker>> CYCLE_TICKER_PROVIDERS = new ArrayList<>();

    private final List<CycleTicker> cycleTickers = new ArrayList<>(CYCLE_TICKER_PROVIDERS.size());

    private boolean firstTimeSetHappened = false;

    private static final HashMap<RegistryKey<World>, CycleTimer> CYCLE_TIMERS = new HashMap<>();

    public final RainTicker rainTicker;
    public final CycleSleep cycleSleep;

    /**
     * Register a CycleTicker provider.
     * This provider will be instantiated for all future CycleTimers,
     * and will be automatically instantiated for all existing CycleTimers
     * @param cycleTickerProvider The cycle ticker provider to register, usually CycleTicker::new
     */
    public static void registerCycleTicker(Supplier<CycleTicker> cycleTickerProvider)
    {
        CYCLE_TICKER_PROVIDERS.add(cycleTickerProvider);

        CYCLE_TIMERS.values().forEach((cycleTimer) -> {
            cycleTimer.cycleTickers.add(cycleTickerProvider.get());
        });
    }

    /**
     * Starts a new cycle, and propagates that message to all registered cycleTickers
     * @param world
     */
    public void startOfCycle(World world)
    {
        selectNextCycleLength(world);

        cycleTickers.forEach((cycleTicker ->
                cycleTicker.startOfCycle(this, world)));
    }

    /**
     * @return A float value normalized such that 0 represents the beginning of the current cycle,
     * and 1 represents the beginning of rain falling. The return value may be less than 0 before a cycle,
     * and greater than 1 after the rain begins
     */
    public float getTimePercentage()
    {
        return 1 - ((float) cycleTimeLeft / cycleLength);
    }

    /**
     * Gets the CycleTimer associated with the provided world registry key
     * @param world
     * @return
     */
    public static CycleTimer getCycleTimer(RegistryKey<World> world) {
        return CYCLE_TIMERS.get(world);
    }

    /**
     * @return A value representing how fast the current cycle will pass, relative to a typical Minecraft day
     */
    public float getTimeMultiplier()
    {
        return 12000f / this.cycleLength;
    }

    /**
     * Converts an MC time value to a RW time value based on the current timesscale
     * @param mcTime
     * @return
     */
    public long convertToRW(long mcTime)
    {
        return (long) (mcTime / getTimeMultiplier());
    }

    /**
     * Converts a RW time value to an MC time value based on the current timescale
     * @param rwTime
     * @return
     */
    public long convertToMC(long rwTime)
    {
        return (long) (rwTime * getTimeMultiplier());
    }

    /**
     * Increments the current cycle by a certain number of ticks
     * @param rwTimeDelta
     */
    public void advanceCycleTimer(long rwTimeDelta)
    {
        if (!firstTimeSetHappened) {
            firstTimeSetHappened = true;
            return;
        }

        this.cycleTimeLeft += rwTimeDelta;
        this.markDirty();
    }

    private CycleTimer() {
        this(-1, -1, 20*60*9, 20*60*15);
    }

    private CycleTimer(int cycleLength, long cycleTimeLeft, int minimumCycleTime, int maximumCycleTime) {
        this.cycleLength = cycleLength;
        this.cycleTimeLeft = cycleTimeLeft;
        this.minimumCycleTime = minimumCycleTime;
        this.maximumCycleTime = maximumCycleTime;

        CYCLE_TICKER_PROVIDERS.forEach((cycleTickerSupplier ->
                cycleTickers.add(cycleTickerSupplier.get())));

        this.cycleSleep = (CycleSleep) cycleTickers.get(0);
        this.rainTicker = (RainTicker) cycleTickers.get(1);
    }

    private void selectNextCycleLength(World world) {
        this.cycleLength = world.getRandom().nextBetween(minimumCycleTime, maximumCycleTime);
        this.cycleTimeLeft = this.cycleLength;

        this.markDirty();
    }

    private static void worldStartTick(World world) {
        CycleTimer cycleTimer = CYCLE_TIMERS.get(world.getRegistryKey());

        if (world.getPlayers().isEmpty())
            return;

        if (!world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
            return;

        cycleTimer.cycleTimeLeft--;

        cycleTimer.markDirty();

        cycleTimer.cycleTickers.forEach(cycleTicker ->
                cycleTicker.onCycleTick(cycleTimer, world));
    }

    public static CycleTimerRequester cycleTimerRequester;

    public static void initialize() {
        ServerWorldEvents.LOAD.register(CycleTimer::load);
        ServerWorldEvents.UNLOAD.register(CycleTimer::unload);
        ServerTickEvents.START_WORLD_TICK.register(CycleTimer::worldStartTick);

        registerCycleTicker(CycleSleep::new);
        registerCycleTicker(RainTicker::new);
    }

    private static void load(MinecraftServer minecraftServer, World world) {
        CycleTimer cycleTimer;

        if (world.isClient) {
            cycleTimer = new CycleTimer();

            if (cycleTimerRequester != null)
                cycleTimerRequester.requestCycleTimer(world.getRegistryKey());
        } else {
            ServerWorld serverWorld = (ServerWorld) world;

            PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
            cycleTimer = persistentStateManager.getOrCreate(CycleTimer::createFromNbt, CycleTimer::new, RainworldMod.MOD_ID);

            if (cycleTimer.cycleLength == -1)
                cycleTimer.selectNextCycleLength(serverWorld);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeRegistryKey(world.getRegistryKey());
            buf.writeInt(cycleTimer.cycleLength);
            buf.writeLong(cycleTimer.cycleTimeLeft);

            for (ServerPlayerEntity player : PlayerLookup.world(serverWorld))
                ServerPlayNetworking.send(player, SyncCycleTimer.SYNC_CYCLE_TIMER_PACKET_ID, new SyncCycleTimer(world.getRegistryKey(), cycleTimer.cycleLength, cycleTimer.cycleTimeLeft).toBuf());
        }

        CYCLE_TIMERS.put(world.getRegistryKey(), cycleTimer);
        cycleTimer.world = world.getRegistryKey();
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
