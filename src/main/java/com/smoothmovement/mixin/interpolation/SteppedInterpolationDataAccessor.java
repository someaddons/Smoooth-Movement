package com.smoothmovement.mixin.interpolation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.entity.SteppedInterpolationHandler$InterpolationData")
public interface SteppedInterpolationDataAccessor
{
    @Accessor("currentStepTicks")
    void smoothmovement$setCurrentStepTicks(float ticks);
}
