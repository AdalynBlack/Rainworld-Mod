package com.rainworldmod.mechanics.sun;

import com.rainworldmod.mechanics.Easing;

public class SunAnimator {
    private static final long DAWN_SUN_TIME = 1000;
    private static final long MID_CYCLE_START_TIME = 3500;
    private static final long MID_CYCLE_END_TIME = 7500;
    private static final long MIDNIGHT_SUN_TIME = 18000;

    private static final double MID_CYCLE_START = 0.1;
    private static final double MID_CYCLE_END = 1.0;
    private static final double MIDNIGHT_SUN_START = 1.2;

    public static long getSimulatedTimeOfDay(float timePercentage)
    {
        if (timePercentage < MID_CYCLE_START)
            return (long) Easing.mapValue(
                    Easing.easeOutCubic(Easing.mapValue(
                            timePercentage,
                            0, MID_CYCLE_START,
                            0, 1)),
                    0, 1,
                    DAWN_SUN_TIME, MID_CYCLE_START_TIME);

        if (timePercentage < MID_CYCLE_END)
            return (long) Easing.mapValue(
                    Easing.easeInOutQuadratic(Easing.mapValue(
                            timePercentage,
                            MID_CYCLE_START, MID_CYCLE_END,
                            0, 1)),
                    0, 1,
                    MID_CYCLE_START_TIME, MID_CYCLE_END_TIME);

        if (timePercentage < MIDNIGHT_SUN_START)
            return (long) Easing.mapValue(
                    Easing.easeInOutCubic(Easing.mapValue(
                            timePercentage,
                            MID_CYCLE_END, MIDNIGHT_SUN_START,
                            0, 1)),
                    0, 1,
                    MID_CYCLE_END_TIME, MIDNIGHT_SUN_TIME);

        return MIDNIGHT_SUN_TIME;
    }
}
