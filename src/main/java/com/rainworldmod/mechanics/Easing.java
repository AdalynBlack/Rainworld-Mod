package com.rainworldmod.mechanics;

// Mathematical easing functions are from easings.net
public class Easing {
    public static double mapValue(double value, double oldMinimum, double oldMaximum, double newMinimum, double newMaximum)
    {
        double oldRange = oldMaximum - oldMinimum;
        double newRange = newMaximum - newMinimum;

        value = (value - oldMinimum) / oldRange;
        value = (value * newRange) + newMinimum;

        return value;
    }

    public static double easeInOutQuadratic(double normalizedProgress)
    {
        if (normalizedProgress < 0.5)
            return 2 * normalizedProgress * normalizedProgress;
        normalizedProgress = -2 * normalizedProgress + 2;
        return 1 - (normalizedProgress * normalizedProgress) / 2;
    }

    public static double easeOutCubic(double normalizedProgress)
    {
        if (normalizedProgress >= 1)
            return 1;
        if (normalizedProgress <= 0)
            return 0;

        normalizedProgress = 1 - normalizedProgress;
        return 1 - (normalizedProgress * normalizedProgress * normalizedProgress);
    }

    public static double easeInOutCubic(double normalizedProgress)
    {
        if (normalizedProgress < 0.5)
            return 4 * normalizedProgress * normalizedProgress * normalizedProgress;
        normalizedProgress = -2 * normalizedProgress + 2;
        return 1 - (normalizedProgress * normalizedProgress * normalizedProgress) / 2;
    }

    private static final double BOUNCE_FACTOR_MULTIPLIER = 7.5625;
    private static final double BOUNCE_FACTOR_DIVISOR = 2.75;
    public static double easeOutBounce(double normalizedProgress)
    {
        if (normalizedProgress >= 1)
            return 1;
        if (normalizedProgress <= 0)
            return 0;

        if(normalizedProgress < 1 / BOUNCE_FACTOR_DIVISOR)
            return BOUNCE_FACTOR_MULTIPLIER * normalizedProgress * normalizedProgress;
        else if(normalizedProgress < 2 / BOUNCE_FACTOR_DIVISOR)
            return BOUNCE_FACTOR_MULTIPLIER * (normalizedProgress -= 1.5 / BOUNCE_FACTOR_DIVISOR) * normalizedProgress + 0.75;
        else if (normalizedProgress < 2.5 / BOUNCE_FACTOR_DIVISOR)
            return BOUNCE_FACTOR_MULTIPLIER * (normalizedProgress -= 2.25 / BOUNCE_FACTOR_DIVISOR) * normalizedProgress + 0.9375;
        else
            return BOUNCE_FACTOR_MULTIPLIER * (normalizedProgress -= 2.625 / BOUNCE_FACTOR_DIVISOR) * normalizedProgress + 0.984375;
    }
}
