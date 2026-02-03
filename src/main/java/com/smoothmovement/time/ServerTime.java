package com.smoothmovement.time;

import net.minecraft.util.Mth;

public class ServerTime
{
    /**
     * Factor of how much slower the last tick was compared to normal
     */
    public static float  slownessFactor = 1.0f;

    /**
     * Factor of how much slower the last tick was compared to normal
     */
    public static float  averageSlownessFactor = 1.0f;

    /**
     * How many extra ticks are needed to catch up to a normal time interval
     */
    public static int    extraTicks     = 0;

    /**
     * Tracks partial ticks
     */
    private static double extraTickTotal = 0;

    /**
     * Artificial lag TPS target, allows simulating low tps
     */
    public static int artificialLagTPS = -1;
    public static int artificialLagBaseMS = 50;

    /**
     * On servertick estimate lag
     *
     * @param tickTimes serverticktimes
     * @param tickCount servertickcount
     */
    public static void onTick(final long[] tickTimes, final int tickCount)
    {
        final double lastTickMs = tickTimes[tickCount % 100] * 1.0E-6D;
        if (lastTickMs > 50 && tickCount > 200)
        {
            slownessFactor = (float) Mth.clamp(lastTickMs / 50, 1.0D, 10.0D);

            float gameTimeDiff = Math.min(slownessFactor - averageSlownessFactor, 2.5f);
            float adjustment = gameTimeDiff > averageSlownessFactor ? 0.35f : 0.08f;
            averageSlownessFactor += (gameTimeDiff - averageSlownessFactor) * adjustment;
            averageSlownessFactor = Mth.clamp(averageSlownessFactor, 1.0f, 20.0f);

            extraTickTotal += slownessFactor - 1.0f;

            extraTicks = (int) extraTickTotal;
            extraTickTotal = extraTickTotal - (extraTicks);
        }
        else
        {
            // Reset to default
            slownessFactor = 1.0f;
            extraTickTotal = 0;
            extraTicks = 0;
        }
    }
}
