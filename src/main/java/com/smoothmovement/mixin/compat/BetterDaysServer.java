package com.smoothmovement.mixin.compat;

import betterdays.time.Time;
import betterdays.time.TimeService;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TimeService.class, remap = false)
public class BetterDaysServer
{
    @Inject(method = "getTimeSpeed", at = @At("RETURN"), cancellable = true, require = 0)
    public void adjustTimeSpeed(final Time time, final CallbackInfoReturnable<Double> cir)
    {
        if (CommonConfiguration.config.getCommonConfig().enableSkyLagAdjustedMovement)
        {
            cir.setReturnValue(cir.getReturnValue() * ServerTime.slownessFactor);
        }
    }
}
