package com.smoothmovement;

import net.minecraft.world.level.entity.EntityTickList;

public interface ClientLevelDeltaTime
{
    int getDeltaTime();

    void setDeltaTime(int deltaTime);

    EntityTickList getTickingEntities();
}
