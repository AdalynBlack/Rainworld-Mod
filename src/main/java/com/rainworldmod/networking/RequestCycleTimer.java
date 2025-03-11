package com.rainworldmod.networking;

import com.rainworldmod.RainworldMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public class RequestCycleTimer implements CustomPayload {
    public static final Identifier REQUEST_CYCLE_TIMER_PACKET_ID = Objects.requireNonNull(Identifier.of(RainworldMod.MOD_ID, "request_world_timer"));

    public static final CustomPayload.Id<RequestCycleTimer> ID = new CustomPayload.Id<>(REQUEST_CYCLE_TIMER_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, RequestCycleTimer> CODEC = PacketCodec.tuple(
            RegistryKey.createPacketCodec(World.OVERWORLD.getRegistryRef()),
            RequestCycleTimer::toWorldKey,
            RequestCycleTimer::new);

    private RegistryKey<World> worldKey;

    public RequestCycleTimer(RegistryKey<World> worldKey) {
        this.worldKey = worldKey;
    }

    public static RegistryKey<World> toWorldKey(RequestCycleTimer timer) {
        return timer.worldKey;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
