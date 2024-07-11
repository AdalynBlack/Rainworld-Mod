package com.rainworldmod.mechanics;

import com.rainworldmod.networking.RequestCycleTimer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class CycleTimerClient extends CycleTimerRequester {
    public void requestCycleTimer(RegistryKey<World> world)
    {
        ClientPlayNetworking.send(RequestCycleTimer.REQUEST_CYCLE_TIMER_PACKET_ID, PacketByteBufs.create());
    }
}
