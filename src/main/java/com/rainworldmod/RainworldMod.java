package com.rainworldmod;

import com.rainworldmod.mechanics.CycleTimerCommand;
import com.rainworldmod.mechanics.WorldTimer;
import com.rainworldmod.networking.RequestWorldTimer;
import com.rainworldmod.networking.SyncWorldTimer;
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
		WorldTimer.initialize();

		// 1.20.1
		/*
		PayloadTypeRegistry.playS2C().register(SyncWorldTimer.ID, SyncWorldTimer.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestWorldTimer.ID, RequestWorldTimer.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestWorldTimer.ID, (payload, context) -> {
		 */

		ServerPlayNetworking.registerGlobalReceiver(RequestWorldTimer.REQUEST_WORLD_TIMER_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				WorldTimer worldTimer = WorldTimer.getWorldTimer(player.getWorld().getRegistryKey());
				ServerPlayNetworking.send(player, SyncWorldTimer.SYNC_WORLD_TIMER_PACKET_ID, new SyncWorldTimer(player.getWorld().getRegistryKey(), worldTimer.cycleLength, worldTimer.cycleTimeLeft).toBuf());
			});
		});

		CycleTimerCommand.initialize();
	}
}
