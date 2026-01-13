package com.smoothmovement;

public interface ClientLevelDeltaTime
{
    double getSlownessFactor();

    void onTimePacket(long newGameTime);
}
