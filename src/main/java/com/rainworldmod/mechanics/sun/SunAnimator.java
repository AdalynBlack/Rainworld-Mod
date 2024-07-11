package com.rainworldmod.mechanics.sun;

import com.rainworldmod.mechanics.Easing;

public class SunAnimator {
    private static final long DAWN_SUN_TIME = 1000;
    private static final long STANDARD_SUN_TIME = 5500;
    private static final long MIDNIGHT_SUN_TIME = 18000;

    private static final double STANDARD_SUN_START = 0.1;
    private static final double STANDARD_SUN_END = 1.0;
    private static final double MIDNIGHT_SUN_START = 1.2;

    public static long getSimulatedTimeOfDay(float timePercentage)
    {
        if (timePercentage < STANDARD_SUN_START)
            return (long) Easing.mapValue(
                    Easing.easeOutCubic(Easing.mapValue(
                            timePercentage,
                            0, STANDARD_SUN_START,
                            0, 1)),
                    0, 1,
                    DAWN_SUN_TIME, STANDARD_SUN_TIME);

        if (timePercentage < STANDARD_SUN_END)
            return STANDARD_SUN_TIME;

        if (timePercentage < MIDNIGHT_SUN_START)
            return (long) Easing.mapValue(
                    Easing.easeInOutCubic(Easing.mapValue(
                            timePercentage,
                            STANDARD_SUN_END, MIDNIGHT_SUN_START,
                            0, 1)),
                    0, 1,
                    STANDARD_SUN_TIME, MIDNIGHT_SUN_TIME);

        return MIDNIGHT_SUN_TIME;
    }
}
