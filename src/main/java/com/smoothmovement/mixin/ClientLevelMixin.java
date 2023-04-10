package com.smoothmovement.mixin;

import com.smoothmovement.ClientLevelDeltaTime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
/**
 * Ticking entity fps culling for all
 */
public class ClientLevelMixin implements ClientLevelDeltaTime
{
    @Shadow @Final private EntityTickList tickingEntities;

    @Override
    public int getDeltaTime()
    {
        return deltaTime;
    }

    @Unique
    private int deltaTime = 1;

    @Override
    public void setDeltaTime(int deltaTime)
    {
        this.deltaTime = deltaTime;
    }

    @Override
    public EntityTickList getTickingEntities()
    {
        return tickingEntities;
    }
}
