package com.rainworldmod;

import com.rainworldmod.mechanics.WorldTimer;
import com.rainworldmod.mechanics.WorldTimerClient;
import com.rainworldmod.networking.SyncWorldTimer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RainworldModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldTimer.worldTimerRequester = new WorldTimerClient();

		ClientPlayNetworking.registerGlobalReceiver(SyncWorldTimer.SYNC_WORLD_TIMER_PACKET_ID, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				SyncWorldTimer worldTimerPaylod = new SyncWorldTimer(buf);
				WorldTimer worldTimer = WorldTimer.getWorldTimer(worldTimerPaylod.worldKey);
				worldTimer.cycleTimeLeft = worldTimerPaylod.cycleTimeLeft;
				worldTimer.cycleLength = worldTimerPaylod.cycleLength;
			});
		});
	}
}
