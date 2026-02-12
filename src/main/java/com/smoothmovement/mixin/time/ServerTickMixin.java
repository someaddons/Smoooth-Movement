package com.smoothmovement.mixin.time;

import com.smoothmovement.SmoothMovement;
import com.smoothmovement.time.ServerTime;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class ServerTickMixin
{

    @Shadow
    public abstract int getTickCount();

    @Shadow @Final private long[] tickTimesNanos;

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getNanos()J", shift = At.Shift.AFTER, ordinal = 0))
    private void onServerTick(final BooleanSupplier p_129871_, final CallbackInfo ci)
    {
        ServerTime.onTick((MinecraftServer) (Object)this,tickTimesNanos, getTickCount());

        if (ServerTime.artificialLagTPS > 0)
        {
            // TPS 20 = 0
            // TPS 10 = 1.0
            // TPS 5 = 3.0
            // TPS 2 = 9.0
            double lagModifier = (20.0 / (ServerTime.artificialLagTPS)) - 1.0;

            if (lagModifier > 0.1)
            {
                try
                {
                    // No lag = sleep 0;
                    // TPS 10 = Avg 50ms
                    // TPS 5 = Avg 150ms
                    // TPS 2 = Avg 450ms
                    final long sleep = (long)
                        (SmoothMovement.rand.nextInt((int) (78 * lagModifier))
                        + SmoothMovement.rand.nextInt((int) (14 * lagModifier))
                        + 7 * lagModifier
                        + ServerTime.artificialLagBaseMS);
                    Thread.sleep(sleep);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
