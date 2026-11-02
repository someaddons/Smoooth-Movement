package com.smoothmovement.mixin.interpolation;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InterpolationHandler.class)
public abstract class InterpolationHandlerMixin
{
    @Shadow
    private int interpolationSteps;

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    private InterpolationHandler.InterpolationData interpolationData;

    @Inject(method = "interpolateTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;"))
    private void adjustSteps(final Vec3 position, final float yRot, final float xRot, final CallbackInfo ci)
    {
        if (!(entity.level() instanceof ClientLevelDeltaTime deltaLevel))
        {
            return;
        }

        if (deltaLevel.getSlownessFactor() <= 1.0 || !entity.isAlive())
        {
            return;
        }

        if ((entity instanceof LivingEntity && CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
            || (entity instanceof AbstractMinecart && CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing))
        {
            interpolationData.steps = Math.clamp(Math.round(interpolationSteps * deltaLevel.getSlownessFactor()), interpolationSteps, interpolationSteps * 10);
        }
    }

    @ModifyVariable(method = "interpolateTo", at = @At("HEAD"), argsOnly = true)
    private Vec3 adjustDeadPosition(final Vec3 position)
    {
        if (entity instanceof LivingEntity && !entity.isAlive() && CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
        {
            return new Vec3(entity.getX(), Math.min(position.y, entity.getY()), entity.getZ());
        }
        return position;
    }
}
