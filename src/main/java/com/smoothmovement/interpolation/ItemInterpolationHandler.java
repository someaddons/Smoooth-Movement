package com.smoothmovement.interpolation;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Delays server corrections while the item continues to simulate its own movement.
 */
public class ItemInterpolationHandler implements InterpolationHandler
{
    private static final double MAX_TARGET_DISTANCE     = 1.4;
    private static final double TELEPORT_DETECTION_DISTANCE = 64 * 64;

    private final ItemEntity entity;
    private Vec3  targetPosition = Vec3.ZERO;
    private int   lerpSteps;
    private float lerpYRot;
    private float lerpXRot;

    public ItemInterpolationHandler(final ItemEntity entity)
    {
        this.entity = entity;
    }

    @Override
    public @Nullable PositionAndRotation target()
    {
        return hasActiveInterpolation() ? PositionAndRotation.of(targetPosition, lerpYRot, lerpXRot) : null;
    }

    @Override
    public boolean interpolateTo(final @Nullable PositionPath path, final float yRot, final float xRot, final boolean hasRotation)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            cancel();
            return false;
        }

        final Vec3 position = path != null ? path.endPosition() : hasActiveInterpolation() ? targetPosition : entity.position();
        final float targetYRot = hasRotation ? yRot : hasActiveInterpolation() ? lerpYRot : entity.getYRot();
        final float targetXRot = hasRotation ? xRot : hasActiveInterpolation() ? lerpXRot : entity.getXRot();
        // Keep the pending delay for rotation-only updates.
        if (hasActiveInterpolation() && targetPosition.equals(position))
        {
            lerpYRot = targetYRot;
            lerpXRot = targetXRot;
            return true;
        }

        lerpSteps = 2;
        if (entity.level() instanceof ClientLevelDeltaTime deltaLevel
            && position.distanceToSqr(entity.position()) < targetPosition.distanceToSqr(entity.position())
            && deltaLevel.getSlownessFactor() > 1.001)
        {
            lerpSteps = (int) Mth.clamp(Math.round(21 * deltaLevel.getSlownessFactor()), 2, 100);
        }

        targetPosition = position;
        lerpYRot = targetYRot;
        lerpXRot = targetXRot;
        interpolate();
        return true;
    }

    @Override
    public void applyPredictedMovement(final Vec3 delta)
    {
        // Keep the server correction fixed while the item simulates its own movement.
    }

    public void lerpMotion(final Vec3 movement)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableItemSmoothing || lerpSteps < 2 || entity.tickCount < 2
            || !(entity.level() instanceof ClientLevelDeltaTime deltaLevel) || deltaLevel.getSlownessFactor() < 1.1)
        {
            entity.setDeltaMovement(movement);
        }
    }

    @Override
    public boolean hasActiveInterpolation()
    {
        return lerpSteps > 0;
    }

    @Override
    public void interpolate()
    {
        if (!CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            cancel();
            return;
        }

        if (lerpSteps <= 0)
        {
            return;
        }

        final double sqDist = targetPosition.distanceToSqr(entity.position());
        if (sqDist > TELEPORT_DETECTION_DISTANCE)
        {
            cancel();
            return;
        }

        if (--lerpSteps == 0)
        {
            if (sqDist > MAX_TARGET_DISTANCE)
            {
                entity.setPos(targetPosition);
            }
            entity.setYRot(lerpYRot % 360.0F);
            entity.setXRot(lerpXRot % 360.0F);
            cancel();
        }
    }

    @Override
    public void cancel()
    {
        lerpSteps = 0;
        targetPosition = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
