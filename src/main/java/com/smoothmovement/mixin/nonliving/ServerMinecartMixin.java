package com.smoothmovement.mixin.nonliving;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractMinecart.class)
public abstract class ServerMinecartMixin extends Entity
{
    @Shadow protected abstract double getMaxSpeed();

    public ServerMinecartMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Redirect(method = "moveAlongTrack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getMaxSpeed()D"))
    private double adjustSpeed(final AbstractMinecart instance)
    {
        double defaultValue = getMaxSpeed();

        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return defaultValue;
        }

        if (!ServerTime.hasLag())
        {
            return defaultValue;
        }

        double maxSpeed = 0.4f;
        final BlockState state = getBlockStateOn();
        if (state.getBlock() instanceof BaseRailBlock railBlock)
        {
            final RailShape shape = state.getValue(railBlock.getShapeProperty());
            if (shape == RailShape.EAST_WEST || shape == RailShape.NORTH_SOUTH)
            {
                Direction direction = null;
                switch (shape)
                {
                    case EAST_WEST -> direction = getDeltaMovement().x > 0 ? Direction.EAST : Direction.WEST;
                    case NORTH_SOUTH -> direction = getDeltaMovement().z > 0 ? Direction.SOUTH : Direction.NORTH;
                }

                final BlockState nextRail = level().getBlockState(blockPosition().relative(direction));
                if (nextRail.getBlock() instanceof BaseRailBlock railBlock2)
                {
                    final RailShape shape2 = nextRail.getValue(railBlock2.getShapeProperty());
                    if (shape2 == RailShape.EAST_WEST || shape2 == RailShape.NORTH_SOUTH)
                    {
                        maxSpeed = 6.4f;
                    }
                }
            }
        }

        return Math.min(maxSpeed, defaultValue * ServerTime.slownessFactor);
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.06D))
    private double adjustValue(final double constant)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }

        return constant * ServerTime.slownessFactor;
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.02D))
    private double adjustValue2(final double constant)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }
        return constant * ServerTime.slownessFactor;
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = -0.02D))
    private double adjustValue3(final double constant)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }
        return constant * ServerTime.slownessFactor;
    }
}
