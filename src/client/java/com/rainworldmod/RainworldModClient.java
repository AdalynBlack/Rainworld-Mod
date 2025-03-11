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

		ClientPlayNetworking.registerGlobalReceiver(SyncCycleTimer.ID, (payload, context) -> context.client().execute(() -> {
            CycleTimer cycleTimer = CycleTimer.getCycleTimer(payload.worldKey);
            cycleTimer.cycleTimeLeft = payload.cycleTimeLeft;
            cycleTimer.cycleLength = payload.cycleLength;
        }));
	}
}
