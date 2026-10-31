package com.smoothmovement.mixin.nonliving;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OldMinecartBehavior.class)
public abstract class ServerMinecartMixin extends MinecartBehavior
{
    protected ServerMinecartMixin(final AbstractMinecart minecart)
    {
        super(minecart);
    }

    @ModifyReturnValue(method = "getMaxSpeed", at = @At("RETURN"))
    private double adjustSpeed(final double defaultValue, final ServerLevel level)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return defaultValue;
        }

        if (!ServerTime.hasLag())
        {
            return defaultValue;
        }

        float maxSpeed = 0.4f;
        final BlockPos pos = minecart.getCurrentBlockPosOrRailBelow();
        final BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BaseRailBlock railBlock)
        {
            final RailShape shape = railBlock.getRailDirection(state, level, pos, minecart);
            if (shape == RailShape.EAST_WEST || shape == RailShape.NORTH_SOUTH)
            {
                Direction direction = null;
                switch (shape)
                {
                    case EAST_WEST -> direction = getDeltaMovement().x > 0 ? Direction.EAST : Direction.WEST;
                    case NORTH_SOUTH -> direction = getDeltaMovement().z > 0 ? Direction.SOUTH : Direction.NORTH;
                }

                final BlockState nextRail = level.getBlockState(pos.relative(direction));
                if (nextRail.getBlock() instanceof BaseRailBlock railBlock2)
                {
                    final RailShape shape2 = railBlock2.getRailDirection(nextRail, level, pos.relative(direction), minecart);
                    if (shape2 == RailShape.EAST_WEST || shape2 == RailShape.NORTH_SOUTH)
                    {
                        maxSpeed = 6.4f;
                    }
                }
            }
        }

        return state.getBlock() instanceof BaseRailBlock ? Math.min(maxSpeed, defaultValue * ServerTime.slownessFactor) : defaultValue;
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.06D))
    private double adjustValue(final double constant)
    {
        if (level().isClientSide() || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }

        return constant * ServerTime.slownessFactor;
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.02D))
    private double adjustValue2(final double constant)
    {
        if (level().isClientSide() || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }
        return constant * ServerTime.slownessFactor;
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = -0.02D))
    private double adjustValue3(final double constant)
    {
        if (level().isClientSide() || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableMinecartLagAdjustedMovement)
        {
            return constant;
        }
        return constant * ServerTime.slownessFactor;
    }
}
