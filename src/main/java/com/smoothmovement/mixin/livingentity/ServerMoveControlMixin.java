package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MoveControl.class)
public class ServerMoveControlMixin
{
    @ModifyVariable(method = "rotlerp", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private float adjustSteps(float steps)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            return steps;
        }

        return steps * ServerTime.slownessFactor;
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 2.500000277905201E-7))
    private double adjustThreshold(final double original)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            return original;
        }

        return 2.500000277905201E-7 * ServerTime.slownessFactor;
    }
}
