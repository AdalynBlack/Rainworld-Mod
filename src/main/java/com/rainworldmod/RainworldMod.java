package com.rainworldmod;

import com.rainworldmod.mechanics.cycle.CycleTimerCommand;
import com.rainworldmod.mechanics.cycle.CycleTimer;
import com.rainworldmod.networking.RequestCycleTimer;
import com.rainworldmod.networking.SyncCycleTimer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainworldMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("rainworld-mod");

	public static final String MOD_ID = "rainworld";

	@Override
	public void onInitialize() {
		AllBlocks.initialize();
		AllBlockEntities.initialize();
		CycleTimer.initialize();

		PayloadTypeRegistry.playS2C().register(SyncCycleTimer.ID, SyncCycleTimer.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestCycleTimer.ID, RequestCycleTimer.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestCycleTimer.ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity player = context.player();
			CycleTimer cycleTimer = CycleTimer.getCycleTimer(player.getWorld().getRegistryKey());
            ServerPlayNetworking.send(player, new SyncCycleTimer(player.getWorld().getRegistryKey(), cycleTimer.cycleLength, cycleTimer.cycleTimeLeft));
        }));

		CycleTimerCommand.initialize();
	}
}
