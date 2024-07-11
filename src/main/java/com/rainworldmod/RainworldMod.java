package com.rainworldmod;

import com.rainworldmod.mechanics.cycle.CycleTimerCommand;
import com.rainworldmod.mechanics.cycle.CycleTimer;
import com.rainworldmod.networking.RequestCycleTimer;
import com.rainworldmod.networking.SyncCycleTimer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

		ServerPlayNetworking.registerGlobalReceiver(RequestCycleTimer.REQUEST_CYCLE_TIMER_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				CycleTimer cycleTimer = CycleTimer.getCycleTimer(player.getWorld().getRegistryKey());
				ServerPlayNetworking.send(player, SyncCycleTimer.SYNC_CYCLE_TIMER_PACKET_ID, new SyncCycleTimer(player.getWorld().getRegistryKey(), cycleTimer.cycleLength, cycleTimer.cycleTimeLeft).toBuf());
			});
		});

		CycleTimerCommand.initialize();
	}
}
