package com.smoothmovement.mixin.time;

import com.smoothmovement.ClientLevelDeltaTime;
import com.smoothmovement.SmoothMovement;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
/**
 * Save time deltas on world basis
 */
public class ClientLevelTimeDeltaMixin implements ClientLevelDeltaTime
{
    @Unique
    private double smoothTickTime = 20.0;

    @Unique
    private long lastPacketTime = -1;

    @Unique
    private double slownessFactor = 1.0;

    @Override
    public double getSlownessFactor()
    {
        return slownessFactor;
    }

    @Override
    public void onTimePacket(long newGameTime)
    {
        if (lastPacketTime == -1)
        {
            lastPacketTime = ((ClientLevel)(Object)this).getGameTime() - 20;
        }

        double raw = (((ClientLevel)(Object)this).getGameTime() - lastPacketTime);
        raw = Math.min(raw, smoothTickTime * 2.5f);

        float adjustment = raw > smoothTickTime ? 0.35f : 0.08f;
        smoothTickTime += (raw - smoothTickTime) * adjustment;

        smoothTickTime = Mth.clamp(smoothTickTime, 20.0f, 120.0f);
        SmoothMovement.LOGGER.warn("RAW:"+(((ClientLevel)(Object)this).getGameTime() - lastPacketTime)+" smooth:"+smoothTickTime);

        //smoothTickTime = smoothTickTime * 0.4 + (((ClientLevel)(Object)this).getGameTime() - lastPacketTime) * 0.6;
        lastPacketTime = newGameTime;
        slownessFactor = smoothTickTime / 20.0;
    }
}
