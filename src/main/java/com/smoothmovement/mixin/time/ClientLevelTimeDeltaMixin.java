package com.smoothmovement.mixin.time;

import com.smoothmovement.Compat;
import com.smoothmovement.SmoothMovement;
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
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
/**
 * Save time deltas on world basis
 */
public abstract class ClientLevelTimeDeltaMixin extends Level implements ClientLevelDeltaTime
{
    /**
     * Server tps metrics
     */
    @Unique
    private double smoothPacketTickInterval = 20.0;
    @Unique
    private double slownessFactor           = 1.0;
    @Unique
    private int serverTPS = 20;

    /**
     * Values for daytime tracking
     */
    @Unique
    private long   lastPacketGameTime       = -1;
    @Unique
    private double visualDayTime      = 0;
    @Unique
    private double visualDayTimeSpeed = 1.0;
    @Unique
    private long   lastServerDayTime  = -1;

    /**
     * Packet statistics
     */
    @Unique
    private int tooFastPackets = 0;
    @Unique
    private int ticksSincePacket = 0;
    @Unique
    int totalPackets = 0;
    @Unique
    private boolean serversideSmoothMovement = false;

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
    public float getVisualDayTimeSpeed()
    {
        return (float) visualDayTimeSpeed;
    }

    @Override
    public void onTimePacket(long newGameTime, final long serverDayTime)
    {
        if (ticksSincePacket == 0)
        {
            // Something sent an extra packet, like hourglass mod
            return;
        }

        if (!serversideSmoothMovement)
        {
            onServerPacketCalculateSlowness();
        }

        if (visualDayTime == 0)
        {
            visualDayTime = serverDayTime;
            lastServerDayTime = serverDayTime;
            return;
        }

        if (lastPacketGameTime == -1)
        {
            lastPacketGameTime = getGameTime() - 20;
        }

        final long timeSinceLastPacket = getGameTime() - lastPacketGameTime;
        if (timeSinceLastPacket == 0)
        {
            return;
        }

        lastPacketGameTime = newGameTime;

        final long elapsedServerDayTime = serverDayTime - lastServerDayTime;
        lastServerDayTime = serverDayTime;

        double serverTimeSpeed = (double) elapsedServerDayTime / timeSinceLastPacket;
        visualDayTimeSpeed = visualDayTimeSpeed * 0.8 + serverTimeSpeed * 0.2;

        if (Math.abs(visualDayTime - serverDayTime) > 600)
        {
            visualDayTimeSpeed = 1.0;
            visualDayTime = serverDayTime;
        }
    }

    @Unique
    private void onServerPacketCalculateSlowness()
    {
        if (totalPackets < 5)
        {
            totalPackets++;
            return;
        }

        if (ticksSincePacket < 20)
        {
            tooFastPackets++;
        }
        else
        {
            if (tooFastPackets > 0)
            {
                tooFastPackets--;
            }
        }

        // Normally the packet is received every 20 ticks, which we use as basis for tps calculations, but some mods like hourglass defeat that assumption.
        final int packetTickInterval = tooFastPackets < 10 ? 20 : 1;

        // Caps the diff and adjusts the interval accordingly
        double packetTimeDiffCapped = Math.min(ticksSincePacket, smoothPacketTickInterval * 2.5f);
        float adjustment = packetTimeDiffCapped > smoothPacketTickInterval ? 0.35f : 0.08f;
        smoothPacketTickInterval += (packetTimeDiffCapped - smoothPacketTickInterval) * adjustment;

        // Caps the interval at between 20 tps and 2 tps
        smoothPacketTickInterval = Mth.clamp(smoothPacketTickInterval, packetTickInterval, packetTickInterval * 10);

        // Calculate the slowness factor, depends on the packet receiving speed
        slownessFactor = smoothPacketTickInterval / packetTickInterval;

        if (serversideSmoothMovement)
        {
            if (!FMLEnvironment.production && Math.abs((20.0 / slownessFactor) - serverTPS) > 2)
            {
                SmoothMovement.LOGGER.warn("large tps diff! client:" + (20.0 / slownessFactor) + " server:" + serverTPS);
            }
        }

        ticksSincePacket = 0;
    }

    @Override
    public void onScoreBoardPacket(int score)
    {
        serverTPS = score;
        serversideSmoothMovement = true;
        onServerPacketCalculateSlowness();
        ticksSincePacket = 0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(final BooleanSupplier p_104727_, final CallbackInfo ci)
    {
        ticksSincePacket++;
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setDayTime(J)V"))
    private void adjustVisualTime(final CallbackInfo ci)
    {
        visualDayTime += visualDayTimeSpeed;
    }

    @Override
    public float getTimeOfDay(float partialTick)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableSkySmoothing || Compat.genesis)
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
}
