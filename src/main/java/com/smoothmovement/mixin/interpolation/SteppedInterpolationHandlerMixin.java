package com.smoothmovement.mixin.interpolation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.AbstractInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SteppedInterpolationHandler;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(SteppedInterpolationHandler.class)
public abstract class SteppedInterpolationHandlerMixin extends AbstractInterpolationHandler
{
    protected SteppedInterpolationHandlerMixin(final Entity entity, final int interpolationSteps)
    {
        super(entity, interpolationSteps);
    }

    @WrapOperation(method = "startInterpolating", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/SteppedInterpolationHandler$InterpolationData;setStartingPoint(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void adjustStartingStep(@Coerce final Object data, final Vec3 position, final float yRot, final float xRot, final Operation<Void> original)
    {
        original.call(data, position, yRot, xRot);
        // Vanilla starts one tick ahead; use the same time scale as subsequent advances.
        ((SteppedInterpolationDataAccessor) data).smoothmovement$setCurrentStepTicks(adjustPlaybackSpeed(1.0F));
    }

    @ModifyExpressionValue(method = "doInterpolate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getRelativeTickSpeed()F"))
    private float adjustPlaybackSpeed(final float ticks)
    {
        if (!entity.isAlive() || !(entity.level() instanceof ClientLevelDeltaTime deltaLevel))
        {
            return ticks;
        }

        if ((entity instanceof LivingEntity && CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
            || (entity instanceof AbstractMinecart && CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing))
        {
            // Keep path durations in server ticks and adapt the entire queue to the current lag.
            return (float) (ticks / Math.clamp(deltaLevel.getSlownessFactor(), 1.0, 10.0));
        }
        return ticks;
    }
}
