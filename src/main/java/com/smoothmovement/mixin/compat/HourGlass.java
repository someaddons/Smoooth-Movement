package com.smoothmovement.mixin.compat;

import com.smoothmovement.config.CommonConfiguration;
import net.lavabucket.hourglass.client.TimeInterpolator;
import net.lavabucket.hourglass.wrappers.ClientLevelWrapper;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TimeInterpolator.class, remap = false)
public abstract class HourGlass
{
    @Shadow
    @Final
    public ClientLevelWrapper level;

    /**
     * Cancel clientside updates in favor of smoothmovements
     *
     * @param event
     * @param ci
     */
    @Inject(method = "onRenderTickEvent", at = @At("HEAD"), cancellable = true)
    private static void cancelEvent(final TickEvent.RenderTickEvent event, final CallbackInfo ci)
    {
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing)
        {
            ci.cancel();
        }
    }

    /**
     * Cancel clientside updates in favor of smoothmovements
     *
     * @param event
     * @param ci
     */
    @Inject(method = "OnClientTickEvent", at = @At("HEAD"), cancellable = true)
    private static void cancelEventTick(final TickEvent.ClientTickEvent event, final CallbackInfo ci)
    {
        if (CommonConfiguration.config.getCommonConfig().enableSkySmoothing)
        {
            ci.cancel();
        }
    }
}
