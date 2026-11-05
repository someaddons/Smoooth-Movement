package com.smoothmovement.mixin.interpolation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.AbstractInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.SteppedInterpolationHandler;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractInterpolationHandler.class)
public abstract class InterpolationHandlerMixin
{
    @Shadow
    protected int interpolationSteps;

    @Shadow
    @Final
    protected Entity entity;

    @WrapOperation(method = "interpolateTo(Lnet/minecraft/world/entity/PositionPath;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AbstractInterpolationHandler;startInterpolating(Lnet/minecraft/world/entity/PositionPath;FF)V"))
    private void adjustSteps(final AbstractInterpolationHandler handler, final PositionPath position, final float yRot, final float xRot, final Operation<Void> original)
    {
        final int steps = interpolationSteps;
        if (!(handler instanceof SteppedInterpolationHandler))
        {
            interpolationSteps = Math.clamp(Math.round(steps * smoothingFactor()), steps, steps * 10);
        }
        try
        {
            original.call(handler, position, yRot, xRot);
        }
        finally
        {
            interpolationSteps = steps;
        }
    }

    @Unique
    private double smoothingFactor()
    {
        if (!(entity.level() instanceof ClientLevelDeltaTime deltaLevel))
        {
            return 1.0;
        }

        if (deltaLevel.getSlownessFactor() <= 1.0 || !entity.isAlive())
        {
            return 1.0;
        }

        if ((entity instanceof LivingEntity && CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
            || (entity instanceof AbstractMinecart && CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing))
        {
            return deltaLevel.getSlownessFactor();
        }
        return 1.0;
    }

    @ModifyVariable(method = "interpolateTo(Lnet/minecraft/world/entity/PositionPath;FF)V", at = @At("HEAD"), argsOnly = true)
    private PositionPath adjustPosition(final PositionPath position)
    {
        if (entity instanceof LivingEntity && !entity.isAlive() && CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
        {
            entity.getInterpolation().cancel();
            return PositionPath.of(new Vec3(entity.getX(), Math.min(position.endPosition().y, entity.getY()), entity.getZ()));
        }

        return position;
    }
}
