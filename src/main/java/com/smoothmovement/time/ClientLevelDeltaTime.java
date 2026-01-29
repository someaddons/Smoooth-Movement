package com.smoothmovement.time;

public interface ClientLevelDeltaTime
{
    double getSlownessFactor();

    double getVisualDayTime();

    void onTimePacket(long newGameTime, final long dayTime);
}
