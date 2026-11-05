package com.smoothmovement.mixin.time;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smoothmovement.Compat;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.AttributeTrackSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Smooth environmental timelines without changing the underlying world clock.
@Mixin(AttributeTrackSampler.class)
public class ClientClockMixin
{
    @Shadow
    @Final
    private ClockManager clockManager;

    @Shadow
    @Final
    private Holder<WorldClock> clock;

    @WrapOperation(method = "applyTimeBased", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ClockInstance;totalTicks()J"))
    private long getVisualTicks(final ClockInstance instance, final Operation<Long> original)
    {
        final ClientLevel level = Minecraft.getInstance().level;
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing && !Compat.genesis
            && level instanceof ClientLevelDeltaTime deltaLevel && level.clockManager() == clockManager
            && level.dimensionType().defaultClock().filter(clock::equals).isPresent())
        {
            final double time = deltaLevel.getVisualDayTime();
            if (!Double.isNaN(time))
            {
                return (long) Math.floor(time);
            }
        }
        return original.call(instance);
    }
}
