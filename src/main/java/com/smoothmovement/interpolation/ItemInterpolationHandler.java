package com.smoothmovement.interpolation;

import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Delays server corrections while the item continues to simulate its own movement.
 */
public class ItemInterpolationHandler extends InterpolationHandler
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
        super(entity);
        this.entity = entity;
    }

    @Override
    public Vec3 position()
    {
        return hasActiveInterpolation() ? targetPosition : entity.position();
    }

    @Override
    public float yRot()
    {
        return hasActiveInterpolation() ? lerpYRot : entity.getYRot();
    }

    @Override
    public float xRot()
    {
        return hasActiveInterpolation() ? lerpXRot : entity.getXRot();
    }

    @Override
    public void interpolateTo(final Vec3 position, final float yRot, final float xRot)
    {
        // Missing packet coordinates are filled from position(), so keep the pending delay for rotation-only updates.
        if (hasActiveInterpolation() && targetPosition.equals(position))
        {
            lerpYRot = yRot;
            lerpXRot = xRot;
            return;
        }

        lerpSteps = 2;
        if (entity.level() instanceof ClientLevelDeltaTime deltaLevel
            && position.distanceToSqr(entity.position()) < targetPosition.distanceToSqr(entity.position())
            && deltaLevel.getSlownessFactor() > 1.001)
        {
            lerpSteps = (int) Mth.clamp(Math.round(21 * deltaLevel.getSlownessFactor()), 2, 100);
        }

        targetPosition = position;
        lerpYRot = yRot;
        lerpXRot = xRot;
        interpolate();
    }

    public void lerpMotion(final Vec3 movement)
    {
        if (lerpSteps < 2 || entity.tickCount < 2
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
        super.cancel();
        lerpSteps = 0;
        targetPosition = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
