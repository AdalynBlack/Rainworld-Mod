package com.rainworldmod.networking;

import com.rainworldmod.RainworldMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public class RequestWorldTimer {//implements CustomPayload {
    public static final Identifier REQUEST_WORLD_TIMER_PACKET_ID = Objects.requireNonNull(Identifier.of(RainworldMod.MOD_ID, "request_world_timer"));

    //public static final Id<RequestWorldTimer> ID = new Id<>(REQUEST_WORLD_TIMER_PACKET_ID);
    /*public static final PacketCodec<RegistryByteBuf, RequestWorldTimer> CODEC = PacketCodec.tuple(
            RegistryKey.createPacketCodec(World.OVERWORLD.getRegistryRef()), RequestWorldTimer::toWorldKey,
            RequestWorldTimer::new);*/

    public RequestWorldTimer() {
    }

    /*
    public static RegistryKey<World> toWorldKey(RequestWorldTimer timer) {
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }*/
}
