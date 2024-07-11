package com.rainworldmod.mechanics;

import com.rainworldmod.networking.RequestWorldTimer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class WorldTimerClient extends WorldTimerRequester {
    public void requestWorldTimer(RegistryKey<World> world)
    {
        ClientPlayNetworking.send(RequestWorldTimer.REQUEST_WORLD_TIMER_PACKET_ID, PacketByteBufs.create());
    }
}
