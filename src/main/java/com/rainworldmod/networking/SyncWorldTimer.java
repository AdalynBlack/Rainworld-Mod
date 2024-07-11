package com.rainworldmod.networking;

import com.rainworldmod.RainworldMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public class SyncWorldTimer {
    public static final Identifier SYNC_WORLD_TIMER_PACKET_ID = Objects.requireNonNull(Identifier.of(RainworldMod.MOD_ID, "sync_world_timer"));

    /*public static final CustomPayload.Id<SyncWorldTimer> ID = new CustomPayload.Id<>(SYNC_WORLD_TIMER_PACKET_ID);

    public static final PacketCodec<RegistryByteBuf, SyncWorldTimer> CODEC = PacketCodec.tuple(
            RegistryKey.createPacketCodec(World.OVERWORLD.getRegistryRef()), SyncWorldTimer::toWorldKey,
            PacketCodecs.VAR_INT, SyncWorldTimer::toCycleLength,
            PacketCodecs.VAR_LONG, SyncWorldTimer::toCycleTimeLeft,
            SyncWorldTimer::new);*/

    public RegistryKey<World> worldKey;
    public int cycleLength;
    public long cycleTimeLeft;

    public SyncWorldTimer(RegistryKey<World> world, int cycleLength, long cycleTimeLeft) {
        this.worldKey = world;
        this.cycleLength = cycleLength;
        this.cycleTimeLeft = cycleTimeLeft;
    }

    public SyncWorldTimer(PacketByteBuf buf)
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

    public static RegistryKey<World> toWorldKey(SyncWorldTimer timer) {
        return timer.worldKey;
    }

    public static int toCycleLength(SyncWorldTimer timer) {
        return timer.cycleLength;
    }

    public static long toCycleTimeLeft(SyncWorldTimer timer) {
        return timer.cycleTimeLeft;
    }

    /*@Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }*/
}
