package com.smoothmovement.mixin.livingentity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ServerLivingEntityMixin extends Entity
{
    @Shadow
    private int noJumpDelay;

    @Unique
    private double slownessFactor = 1.0;
    @Unique
    private Vec3   acceleration   = Vec3.ZERO;
    @Unique
    private Vec3   travelVelocity = Vec3.ZERO;
    @Unique
    private double groundGravity;
    @Unique
    private double groundDrag;

    public ServerLivingEntityMixin(final EntityType<?> type, final Level level)
    {
        super(type, level);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void smoothmovement$resetFluidJump(final CallbackInfo ci)
    {
        acceleration = Vec3.ZERO;
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpInFluid(Lnet/neoforged/neoforge/fluids/FluidType;)V"))
    private void smoothmovement$captureFluidJump(final LivingEntity entity, final FluidType fluid, final Operation<Void> original)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            original.call(entity, fluid);
            return;
        }

        final Vec3 before = getDeltaMovement();
        original.call(entity, fluid);
        acceleration = getDeltaMovement().subtract(before);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void smoothmovement$beginTravel(final Vec3 input, final CallbackInfo ci)
    {
        slownessFactor = 1.0;
        if (level().isClientSide || (Object) this instanceof Player || isPassenger() || !isAlive() || !isControlledByLocalInstance() || getControllingPassenger() instanceof Player
            || !CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement || !ServerTime.hasLag())
        {
            return;
        }

        slownessFactor = ServerTime.slownessFactor;
        travelVelocity = getDeltaMovement();
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void smoothmovement$endTravel(final Vec3 input, final CallbackInfo ci)
    {
        if (slownessFactor > 1.0 && noJumpDelay > 0)
        {
            noJumpDelay = Math.max(0, noJumpDelay - ServerTime.extraTicks);
        }

        slownessFactor = 1.0;
    }

    @WrapOperation(method = {"travel",
        "handleRelativeFrictionAndCalculateMovement"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"))
    private void smoothmovement$captureAcceleration(final LivingEntity entity, final float speed, final Vec3 input, final Operation<Void> original)
    {
        if (slownessFactor <= 1.0)
        {
            original.call(entity, speed, input);
            return;
        }

        final Vec3 before = getDeltaMovement();
        original.call(entity, speed, input);
        acceleration = acceleration.add(getDeltaMovement().subtract(before));
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private void smoothmovement$captureGroundPhysics(final Vec3 input, final CallbackInfo ci, @Local(ordinal = 0) final double gravity, @Local(ordinal = 1) final float drag)
    {
        groundGravity = gravity;
        groundDrag = ((LivingEntity) (Object) this).shouldDiscardFriction() ? 1.0 : drag;
    }

    @ModifyArg(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"), index = 1)
    private Vec3 smoothmovement$moveOnLand(final Vec3 movement)
    {
        if (slownessFactor <= 1.0)
        {
            return movement;
        }

        final LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.onClimbable())
        {
            // Climbing clamps velocity each tick rather than following free-fall equations.
            return movement.scale(slownessFactor);
        }

        double verticalDrag = entity.shouldDiscardFriction() ? 1.0 : entity instanceof FlyingAnimal ? groundDrag : (double) 0.98F;
        double verticalForce = -groundGravity * verticalDrag;
        final var levitation = entity.getEffect(MobEffects.LEVITATION);
        if (levitation != null)
        {
            verticalForce = 0.05 * (levitation.getAmplifier() + 1) * 0.2 * verticalDrag;
            verticalDrag *= 0.8;
        }
        return smoothmovement$adjustDeltaMovement(movement, acceleration.add(0.0, verticalForce, 0.0), groundDrag, verticalDrag);
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", ordinal = 0), index = 1)
    private Vec3 smoothmovement$moveInWater(final Vec3 movement, @Local(ordinal = 0) final double gravity, @Local(ordinal = 0) final float drag)
    {
        if (slownessFactor <= 1.0)
        {
            return movement;
        }

        final double verticalForce = isSprinting() ? 0.0 : -gravity / 16.0;
        return smoothmovement$adjustDeltaMovement(movement, acceleration.add(0.0, verticalForce, 0.0), drag, 0.8F);
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", ordinal = 1), index = 1)
    private Vec3 smoothmovement$moveInLava(final Vec3 movement, @Local(ordinal = 0) final double gravity)
    {
        if (slownessFactor <= 1.0)
        {
            return movement;
        }

        final boolean shallow = getFluidHeight(FluidTags.LAVA) <= getFluidJumpThreshold();
        final double verticalDrag = shallow ? (double) 0.8F : 0.5;
        final double verticalForce = -gravity / 4.0 - (shallow && !isSprinting() ? gravity / 16.0 : 0.0);
        return smoothmovement$adjustDeltaMovement(movement, acceleration.add(0.0, verticalForce, 0.0), 0.5, verticalDrag);
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", ordinal = 2), index = 1)
    private Vec3 smoothmovement$moveGliding(final Vec3 movement)
    {
        if (slownessFactor <= 1.0)
        {
            return movement;
        }

        final Vec3 force = movement.subtract(travelVelocity.multiply(0.99F, 0.98F, 0.99F));
        return smoothmovement$adjustDeltaMovement(movement, force, 0.99F, 0.98F);
    }

    @Unique
    private Vec3 smoothmovement$adjustDeltaMovement(final Vec3 movement, final Vec3 increment, final double horizontalDrag, final double verticalDrag)
    {
        // Iterative velocity changes per extra step, e.g. applying drag the necessary amount of times
        final double extraTime = slownessFactor - 1.0;
        final double horizontalLoss = Math.abs(1.0 - horizontalDrag) < 1.0E-7 ? 0.0 : 1.0 - horizontalDrag;
        final double verticalLoss = Math.abs(1.0 - verticalDrag) < 1.0E-7 ? 0.0 : 1.0 - verticalDrag;

        final double horizontalVelocitySum = horizontalLoss == 0.0 ? extraTime : (1.0 - Math.pow(horizontalDrag, extraTime)) / horizontalLoss;
        final double verticalVelocitySum = verticalLoss == 0.0 ? extraTime : (1.0 - Math.pow(verticalDrag, extraTime)) / verticalLoss;

        final double horizontalDistanceSum = horizontalLoss == 0.0 ? slownessFactor * extraTime * 0.5 : (slownessFactor - (1.0 + horizontalDrag * horizontalVelocitySum)) / horizontalLoss;
        final double verticalDistanceSum = verticalLoss == 0.0 ? slownessFactor * extraTime * 0.5 : (slownessFactor - (1.0 + verticalDrag * verticalVelocitySum)) / verticalLoss;

        final double changeX = increment.x - horizontalLoss * movement.x;
        final double changeY = increment.y - verticalLoss * movement.y;
        final double changeZ = increment.z - horizontalLoss * movement.z;

        // Supply the velocity for the last movement
        setDeltaMovement(new Vec3(movement.x + horizontalVelocitySum * changeX,
            movement.y + verticalVelocitySum * changeY,
            movement.z + horizontalVelocitySum * changeZ));
        return new Vec3(slownessFactor * movement.x + horizontalDistanceSum * changeX,
            slownessFactor * movement.y + verticalDistanceSum * changeY,
            slownessFactor * movement.z + horizontalDistanceSum * changeZ);
    }
}
