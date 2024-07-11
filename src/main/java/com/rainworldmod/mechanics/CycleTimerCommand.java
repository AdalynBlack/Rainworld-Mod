package com.rainworldmod.mechanics;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CycleTimerCommand {
    public static void initialize()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("cycle")
                .then(literal("config")
                        .requires(context -> context.hasPermissionLevel(2))
                        .then(literal("minCycleTime")
                                .then(literal("query")
                                        .executes(context -> {
                                            final WorldTimer worldTimer = WorldTimer.getWorldTimer(context.getSource().getWorld().getRegistryKey());
                                            return worldTimer.minimumCycleTime;
                                        }))
                                .then(argument("time", TimeArgumentType.time())
                                        .executes(context -> {
                                            final int ticks = IntegerArgumentType.getInteger(context, "time");
                                            final WorldTimer worldTimer = WorldTimer.getWorldTimer(context.getSource().getWorld().getRegistryKey());

                                            worldTimer.minimumCycleTime = ticks;
                                            worldTimer.markDirty();

                                            if (ticks > worldTimer.maximumCycleTime) {
                                                context.getSource().sendError(Text.literal("Unable to set minimumCycleTime higher than maximumCycleTime!"));
                                                return -ticks;
                                            }

                                            if (ticks < 0) {
                                                context.getSource().sendError(Text.literal("Unable to set minimumCycleTime to a value less than zero!"));
                                                return ticks;
                                            }

                                            context.getSource().sendFeedback(() -> Text.literal("Set minimumCycleTime to %s ticks".formatted(ticks)), false);
                                            return ticks;
                                        })))
                        .then(literal("maxCycleTime")
                                .then(literal("query")
                                        .executes(context -> {
                                            final WorldTimer worldTimer = WorldTimer.getWorldTimer(context.getSource().getWorld().getRegistryKey());
                                            return worldTimer.maximumCycleTime;
                                        }))
                                .then(argument("time", TimeArgumentType.time())
                                        .executes(context -> {
                                            final int ticks = IntegerArgumentType.getInteger(context, "time");
                                            final WorldTimer worldTimer = WorldTimer.getWorldTimer(context.getSource().getWorld().getRegistryKey());

                                            worldTimer.maximumCycleTime = ticks;
                                            worldTimer.markDirty();

                                            if (worldTimer.minimumCycleTime > ticks) {
                                                context.getSource().sendError(Text.literal("Unable to set maximumCycleTime lower than minimumCycleTime!"));
                                                return -ticks;
                                            }

                                            if (ticks < 0) {
                                                context.getSource().sendError(Text.literal("Unable to set maximumCycleTime to a value less than zero!"));
                                                return ticks;
                                            }

                                            context.getSource().sendFeedback(() -> Text.literal("Set maximumCycleTime to %s ticks".formatted(ticks)), false);

                                            return ticks;
                                        })))
                        .then(literal("cycleTime")
                                .then(argument("minTime", TimeArgumentType.time())
                                        .then(argument("maxTime", TimeArgumentType.time())
                                                .executes(context -> {
                                                    final int minTicks = IntegerArgumentType.getInteger(context, "minTime");
                                                    final int maxTicks = IntegerArgumentType.getInteger(context, "maxTime");
                                                    final WorldTimer worldTimer = WorldTimer.getWorldTimer(context.getSource().getWorld().getRegistryKey());

                                                    worldTimer.minimumCycleTime = minTicks;
                                                    worldTimer.maximumCycleTime = maxTicks;
                                                    worldTimer.markDirty();

                                                    if (minTicks > maxTicks) {
                                                        context.getSource().sendError(Text.literal("Unable to set maximumCycleTime lower than minimumCycleTime!"));
                                                        return maxTicks - minTicks;
                                                    }

                                                    if (minTicks < 0) {
                                                        context.getSource().sendError(Text.literal("Unable to set minimumCycleTime to a value less than zero!"));
                                                        return minTicks;
                                                    }

                                                    // maxTicks < 0 can never happen

                                                    context.getSource().sendFeedback(() -> Text.literal("Set cyleTime to %s-%s ticks".formatted(minTicks, maxTicks)), false);

                                                    return maxTicks - minTicks;
                                                })))))
                .then(literal("reset")
                        .requires(context -> context.hasPermissionLevel(2))
                        .executes(context -> {
                            final World world = context.getSource().getWorld();
                            final WorldTimer worldTimer = WorldTimer.getWorldTimer(world.getRegistryKey());
                            worldTimer.SelectNextRainTime(world);
                            return 1;
                        }))));
    }
}
