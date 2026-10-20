package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LookControl.class)
public class ServerLookControlMixin
{
    /**
     * Scale rotation step limits, but not the calls which cap the rotation instead
     */
    @ModifyArg(method = {"tick", "lambda$tick$0"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/LookControl;rotateTowards(FFF)F"), index = 2)
    private float adjustYawSteps(final float steps)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            return steps;
        }

        return (float) (steps * ServerTime.slownessFactor);
    }
}
