package com.smoothmovement.mixin.time;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.smoothmovement.Compat;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Daytime now advances in the server clock, once for all dimensions.
@Mixin(targets = "net.minecraft.world.clock.ServerClockManager$ServerClockInstance")
public abstract class ServerLevelDayTimeMixin
{
    @Shadow
    @Final
    private Holder<WorldClock> holder;

    @ModifyExpressionValue(method = "tick(Z)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/clock/ServerClockManager$ServerClockInstance;rate:F"))
    private float adjustDayTime(final float rate)
    {
        if (ServerTime.extraTicks > 0 && CommonConfiguration.config.getCommonConfig().enableSkyLagAdjustedMovement && !Compat.hourglass && !Compat.genesis
            && holder != null && holder.is(WorldClocks.OVERWORLD))
        {
            return rate * (1 + ServerTime.extraTicks);
        }
        return rate;
    }
}
