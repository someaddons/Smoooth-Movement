package com.smoothmovement.time;

public interface ClientLevelDeltaTime
{
    /**
     * Factor of how much the server is slower than the client
     * @return
     */
    double getSlownessFactor();

    /**
     * Callback on time packet arrival
     * @param newGameTime
     * @param dayTime
     * @param clockRate server clock speed, or zero while paused
     */
    void onTimePacket(long newGameTime, final long dayTime, final float clockRate);

    double getVisualDayTime();

    /**
     * Get the daytime speed modifier
     * @return
     */
    float getVisualDayTimeSpeed();

    /**
     * Callback on custom scoreboard packet arrival
     * @param score
     */
    void onScoreBoardPacket(int score);
}
