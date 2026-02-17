package com.smoothmovement.time;

import com.smoothmovement.SmoothMovement;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.Mth;

public class ServerTime
{
    public static final String SCOREBOARD_TPS = "smoothmovementtps";
    /**
     * Factor of how much slower the last tick was compared to normal
     */
    public static       float  slownessFactor = 1.0f;

    /**
     * Factor of how much slower the last tick was compared to normal
     */
    public static float averageSlownessFactor = 1.0f;

    /**
     * How many extra ticks are needed to catch up to a normal time interval
     */
    public static int extraTicks = 0;

    /**
     * Tracks partial ticks
     */
    private static double extraTickTotal = 0;

    /**
     * Artificial lag TPS target, allows simulating low tps
     */
    public static int artificialLagTPS    = -1;
    public static int artificialLagBaseMS = 50;

    private static int packetCounter = 0;

    /**
     * Check if the server is lagging
     *
     * @return
     */
    public static boolean hasLag()
    {
        return slownessFactor > 1.0f;
    }

    /**
     * On servertick estimate lag
     *
     * @param tickTimes serverticktimes
     * @param tickCount servertickcount
     */
    public static void onTick(final MinecraftServer server, final long[] tickTimes, final int tickCount)
    {
        final double lastTickMs = tickTimes[tickCount % 100] * 1.0E-6D;
        if (lastTickMs > 50 && tickCount > 200)
        {
            slownessFactor = (float) Mth.clamp(lastTickMs / 50, 1.0D, 10.0D);

            averageSlownessFactor = averageSlownessFactor * 0.8f + slownessFactor * 0.2f;
            extraTickTotal += slownessFactor - 1.0f;

            extraTicks = (int) extraTickTotal;
            extraTickTotal = extraTickTotal - (extraTicks);
        }
        else
        {
            // Reset to default
            slownessFactor = 1.0f;
            averageSlownessFactor = 1.0f;
            extraTickTotal = 0;
            extraTicks = 0;
        }

        // When on serverside send a custom scoreboard packet to the client, allowing the client to use a more secure way of gauging server tps
        if (packetCounter++ == 20)
        {
            server.getPlayerList()
                .broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, SCOREBOARD_TPS, SmoothMovement.MODID, Math.round(20 / averageSlownessFactor)));
            packetCounter = 0;
        }
    }
}
