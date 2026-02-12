package com.smoothmovement.mixin.item;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.smoothmovement.SmoothMovement.getDistanceSquared;

@Mixin(ItemEntity.class)
public abstract class ClientItemEntityMixin extends Entity
{
    @Unique
    private double lerpX;
    @Unique
    private double lerpZ;
    @Unique
    private double lerpY;
    @Unique
    private int    positionDelayTicks;
    @Unique
    private float  lerpYRot;
    @Unique
    private float  lerpXRot;

    public ClientItemEntityMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
        lerpX = getX();
        lerpY = getY();
        lerpZ = getZ();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int p_19901_)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            super.lerpTo(x, y, z, yRot, xRot, p_19901_);
            return;
        }

        //level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        positionDelayTicks = 2;
        if (level() instanceof ClientLevelDeltaTime deltaLevel
            && (getDistanceSquared(x, y, z, getX(), getY(), getZ()) < getDistanceSquared(lerpX, lerpY, lerpZ, getX(), getY(), getZ()))
            && deltaLevel.getSlownessFactor() > 1.001)
        {
            positionDelayTicks = (int) Mth.clamp(Math.round(21 * (deltaLevel.getSlownessFactor())), 2, 100);
        }

        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        checkUpdatePos();
    }

    @Override
    public void lerpMotion(double x, double y, double z)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            super.lerpMotion(x, y, z);
            return;
        }

        if (positionDelayTicks < 2 || tickCount < 2 || (level() instanceof ClientLevelDeltaTime deltaLevel && deltaLevel.getSlownessFactor() < 1.1)
            || !(level() instanceof ClientLevelDeltaTime))
        {
            this.setDeltaMovement(x, y, z);
        }
    }

    @Unique
    private void checkUpdatePos()
    {
        if (positionDelayTicks > 0 && --this.positionDelayTicks == 0)
        {
            if (getDistanceSquared(lerpX, lerpY, lerpZ, getX(), getY(), getZ()) > 1)
            {
                this.setPos(lerpX, lerpY, lerpZ);
            }
            this.setRot(lerpYRot, lerpXRot);
            lerpX = Double.POSITIVE_INFINITY;
            lerpY = Double.POSITIVE_INFINITY;
            lerpZ = Double.POSITIVE_INFINITY;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkUpdatePos(final CallbackInfo ci)
    {
        if (!level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            return;
        }

        checkUpdatePos();
    }
}
