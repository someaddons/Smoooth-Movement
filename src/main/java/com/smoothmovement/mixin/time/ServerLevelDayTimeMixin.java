package com.smoothmovement.mixin.time;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.smoothmovement.Compat;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Daytime now advances in the server clock, once for all dimensions.
@Mixin(targets = "net.minecraft.world.clock.ServerClockManager$ClockInstance")
public abstract class ServerLevelDayTimeMixin
{
    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/clock/ServerClockManager$ClockInstance;rate:F"))
    private float adjustDayTime(final float rate)
    {
        if (ServerTime.extraTicks > 0 && CommonConfiguration.config.getCommonConfig().enableSkyLagAdjustedMovement && !Compat.hourglass && !Compat.genesis)
        {
            return rate * (1 + ServerTime.extraTicks);
        }
        return rate;
    }
}
