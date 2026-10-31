package com.smoothmovement.mixin.time;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smoothmovement.Compat;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.AttributeTrackSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Smooth environmental timelines without changing the underlying world clock.
@Mixin(AttributeTrackSampler.class)
public class ClientClockMixin
{
    @WrapOperation(method = "applyTimeBased", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ClockManager;getTotalTicks(Lnet/minecraft/core/Holder;)J"))
    private long getVisualTicks(final ClockManager clocks, final Holder<WorldClock> clock, final Operation<Long> original)
    {
        final ClientLevel level = Minecraft.getInstance().level;
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing && !Compat.genesis
            && level instanceof ClientLevelDeltaTime deltaLevel && level.clockManager() == clocks
            && level.dimensionType().defaultClock().filter(clock::equals).isPresent())
        {
            final double time = deltaLevel.getVisualDayTime();
            if (!Double.isNaN(time))
            {
                return (long) Math.floor(time);
            }
        }
        return original.call(clocks, clock);
    }
}
