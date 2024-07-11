package com.rainworldmod.networking;

import com.rainworldmod.RainworldMod;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class RequestCycleTimer {//implements CustomPayload {
    public static final Identifier REQUEST_CYCLE_TIMER_PACKET_ID = Objects.requireNonNull(Identifier.of(RainworldMod.MOD_ID, "request_world_timer"));

    public RequestCycleTimer() {
    }
}
