package com.rainworldmod.mechanics.sun;

import com.rainworldmod.mechanics.Easing;

public class SunAnimator {
    public static final long DAWN_SUN_TIME = 1000;
    public static final long MID_CYCLE_START_TIME = 3500;
    public static final long MID_CYCLE_END_TIME = 7500;
    public static final long NIGHT_CYCLE_START_TIME = 12000 + MID_CYCLE_START_TIME;
    public static final long NIGHT_CYCLE_END_TIME = 12000 + MID_CYCLE_END_TIME;
    public static final long PRE_CYCLE_SUN_TIME = NIGHT_CYCLE_END_TIME - 24000;

    public static final double MID_CYCLE_START = 0.1;
    public static final double MID_CYCLE_END = 1.0;
    public static final double NIGHT_CYCLE_START = 1.025;
    public static final double NIGHT_CYCLE_END = 1.05;
    public static final double PRE_CYCLE_START = -0.05;

    public static long getSimulatedTimeOfDay(float timePercentage)
    {
        if (timePercentage < 0)
            return (long) (Easing.mapValue(
                    Easing.easeInCubic(Easing.mapValue(
                            timePercentage,
                            PRE_CYCLE_START, 0,
                            0, 1)),
                    0, 1,
                    PRE_CYCLE_SUN_TIME, DAWN_SUN_TIME) + 24000) % 24000;

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

        if (timePercentage < NIGHT_CYCLE_START)
            return (long) Easing.mapValue(
                    Easing.easeInOutCubic(Easing.mapValue(
                            timePercentage,
                            MID_CYCLE_END, NIGHT_CYCLE_START,
                            0, 1)),
                    0, 1,
                    MID_CYCLE_END_TIME, NIGHT_CYCLE_START_TIME);

        if (timePercentage < NIGHT_CYCLE_END)
            return (long) Easing.mapValue(
                    Easing.easeInOutCubic(Easing.mapValue(
                            timePercentage,
                            NIGHT_CYCLE_START, NIGHT_CYCLE_END,
                            0, 1)),
                    0, 1,
                    NIGHT_CYCLE_START_TIME, NIGHT_CYCLE_END_TIME);

        return NIGHT_CYCLE_END_TIME;
    }
}
