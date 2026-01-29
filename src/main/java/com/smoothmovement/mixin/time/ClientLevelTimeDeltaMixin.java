package com.smoothmovement.mixin.time;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
/**
 * Save time deltas on world basis
 */
public abstract class ClientLevelTimeDeltaMixin extends Level implements ClientLevelDeltaTime
{
    @Unique
    private double smoothTickTime = 20.0;
    @Unique
    private long   lastPacketTime = -1;
    @Unique
    private double slownessFactor = 1.0;

    @Unique
    private double visualDayTime      = 0;
    @Unique
    private double visualDayTimeSpeed = 1.0;
    @Unique
    private long   lastServerDayTime  = -1;

    protected ClientLevelTimeDeltaMixin(
        final WritableLevelData p_270739_,
        final ResourceKey<Level> p_270683_,
        final RegistryAccess p_270200_,
        final Holder<DimensionType> p_270240_,
        final Supplier<ProfilerFiller> p_270692_,
        final boolean p_270904_,
        final boolean p_270470_,
        final long p_270248_,
        final int p_270466_)
    {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Override
    public double getSlownessFactor()
    {
        return slownessFactor;
    }

    @Override
    public double getVisualDayTime()
    {
        return slownessFactor;
    }

    @Override
    public void onTimePacket(long newGameTime, final long serverDayTime)
    {
        if (lastPacketTime == -1)
        {
            lastPacketTime = getGameTime() - 20;
        }

        final long timeSinceLastPacket = getGameTime() - lastPacketTime;
        lastPacketTime = newGameTime;

        double gameTimeDiff = Math.min(timeSinceLastPacket, smoothTickTime * 2.5f);

        float adjustment = gameTimeDiff > smoothTickTime ? 0.35f : 0.08f;
        smoothTickTime += (gameTimeDiff - smoothTickTime) * adjustment;

        smoothTickTime = Mth.clamp(smoothTickTime, 20.0f, 120.0f);

        slownessFactor = smoothTickTime / 20.0;

        if (visualDayTime == 0)
        {
            visualDayTime = serverDayTime;
            lastServerDayTime = serverDayTime;
            return;
        }

        final long elapsedServerDayTime = serverDayTime - lastServerDayTime;
        lastServerDayTime = serverDayTime;

        double serverTimeSpeed = (double) elapsedServerDayTime / timeSinceLastPacket;
        visualDayTimeSpeed = visualDayTimeSpeed * 0.8 + serverTimeSpeed * 0.2;

        if (Math.abs(visualDayTime - serverDayTime) > 2400)
        {
            visualDayTimeSpeed = 1.0;
            visualDayTime = serverDayTime;
        }
    }

    @Override
    public float getTimeOfDay(float partialTick)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableSkySmoothing)
        {
            return super.getTimeOfDay(partialTick);
        }

        double time = visualDayTime + partialTick * visualDayTimeSpeed;
        double d0 = Mth.frac(time / 24000.0D - 0.25D);
        double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
        float smoothTimeValue = (float) (d0 * 2.0D + d1) / 3.0F;

        float dimensionValue = this.dimensionType().timeOfDay((long) time);
        if (Math.abs(smoothTimeValue - dimensionValue) > 0.01)
        {
            return dimensionValue;
        }

        return smoothTimeValue;
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"))
    private void adjustVisualTime(final CallbackInfo ci)
    {
        visualDayTime += visualDayTimeSpeed;
    }
}
