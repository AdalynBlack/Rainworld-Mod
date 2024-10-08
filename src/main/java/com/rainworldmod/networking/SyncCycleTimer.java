package com.rainworldmod.networking;

import com.rainworldmod.RainworldMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public class SyncCycleTimer {
    public static final Identifier SYNC_CYCLE_TIMER_PACKET_ID = Objects.requireNonNull(Identifier.of(RainworldMod.MOD_ID, "sync_world_timer"));

    public RegistryKey<World> worldKey;
    public int cycleLength;
    public long cycleTimeLeft;

    public SyncCycleTimer(RegistryKey<World> world, int cycleLength, long cycleTimeLeft) {
        this.worldKey = world;
        this.cycleLength = cycleLength;
        this.cycleTimeLeft = cycleTimeLeft;
    }

    public SyncCycleTimer(PacketByteBuf buf)
    {
        this.worldKey = buf.readRegistryKey(RegistryKey.ofRegistry(World.OVERWORLD.getRegistry()));
        this.cycleLength = buf.readInt();
        this.cycleTimeLeft = buf.readLong();
    }

    public PacketByteBuf toBuf()
    {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeRegistryKey(this.worldKey);
        buf.writeInt(this.cycleLength);
        buf.writeLong(this.cycleTimeLeft);

        return buf;
    }

    public static RegistryKey<World> toWorldKey(SyncCycleTimer timer) {
        return timer.worldKey;
    }

    public static int toCycleLength(SyncCycleTimer timer) {
        return timer.cycleLength;
    }

    public static long toCycleTimeLeft(SyncCycleTimer timer) {
        return timer.cycleTimeLeft;
    }
}
