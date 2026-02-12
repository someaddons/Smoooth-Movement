package com.smoothmovement.mixin.compat;

import betterdays.client.TimeInterpolator;
import com.smoothmovement.config.CommonConfiguration;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TimeInterpolator.class, remap = false)
public abstract class BetterDays
{
    /**
     * Cancel clientside updates in favor of smoothmovements
     *
     * @param ci
     */
    @Inject(method = "onRenderTickEvent", at = @At("HEAD"), cancellable = true)
    private static void cancelEvent(final float renderTickTime, final CallbackInfo ci)
    {
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing)
        {
            ci.cancel();
        }
    }

    /**
     * Cancel clientside updates in favor of smoothmovements
     *
     * @param ci
     */
    @Inject(method = "onClientTickEvent", at = @At("HEAD"), cancellable = true)
    private static void cancelEventTick(final Minecraft minecraft, final CallbackInfo ci)
    {
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing)
        {
            ci.cancel();
        }
    }
}
