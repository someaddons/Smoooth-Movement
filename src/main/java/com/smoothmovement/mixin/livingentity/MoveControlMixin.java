package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.SmoothMovement;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoveControl.class)
public class MoveControlMixin
{
    @Shadow protected double speedModifier;

    @ModifyVariable(method = "rotlerp", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private float adjustSteps(float steps)
    {
        return steps * SmoothMovement.slownessFactor;
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 2.500000277905201E-7))
    private double adjustThreshold(final double original)
    {
        return  2.500000277905201E-7 * SmoothMovement.slownessFactor;
    }
}
