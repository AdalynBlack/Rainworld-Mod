package com.rainworldmod;

import com.rainworldmod.mechanics.cycle.CycleTimer;
import com.rainworldmod.mechanics.cycle.CycleTimerClient;
import com.rainworldmod.networking.SyncCycleTimer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RainworldModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CycleTimer.cycleTimerRequester = new CycleTimerClient();

		ClientPlayNetworking.registerGlobalReceiver(SyncCycleTimer.SYNC_CYCLE_TIMER_PACKET_ID, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				SyncCycleTimer cycleTimerPayload = new SyncCycleTimer(buf);
				CycleTimer cycleTimer = CycleTimer.getCycleTimer(cycleTimerPayload.worldKey);
				cycleTimer.cycleTimeLeft = cycleTimerPayload.cycleTimeLeft;
				cycleTimer.cycleLength = cycleTimerPayload.cycleLength;
			});
		});
	}
}
