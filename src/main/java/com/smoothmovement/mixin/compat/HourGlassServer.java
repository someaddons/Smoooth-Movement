package com.smoothmovement.mixin.compat;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.lavabucket.hourglass.time.Time;
import net.lavabucket.hourglass.time.TimeService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TimeService.class, remap = false)
public class HourGlassServer
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
