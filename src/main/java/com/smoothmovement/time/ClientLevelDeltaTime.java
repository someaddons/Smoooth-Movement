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
     */
    void onTimePacket(long newGameTime, final long dayTime);

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
