package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClientEntityMovementMixin extends Entity
{
    @Shadow
    protected int lerpSteps;
    @Shadow
    protected int lerpHeadSteps;

    /*
     * Default update rate of entity type
     */
    @Unique
    private int defaultLerp = 3;

    @Shadow
    public abstract void remove(final RemovalReason p_276115_);

    @Shadow
    public abstract boolean isAlive();

    @Shadow
    protected double lerpX;

    @Shadow
    protected double lerpZ;

    @Shadow
    protected double lerpY;

    public ClientEntityMovementMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(final EntityType entityType, final Level p_20967_, final CallbackInfo ci)
    {
        defaultLerp = Math.max(3, entityType.updateInterval());
    }

    @Inject(method = "lerpTo", at = @At("TAIL"))
    private void onPosLerp(
        final double x,
        final double y,
        final double z,
        final float p_20980_,
        final float p_20981_,
        final int steps,
        final CallbackInfo ci)
    {
         if (!CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
         {
             return;
         }

        //level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        lerpSteps = defaultLerp;
        if (level() instanceof ClientLevelDeltaTime deltaLevel)
        {
            lerpSteps = (int) Math.max(3, Math.min(30, Math.round(defaultLerp * deltaLevel.getSlownessFactor())));
        }

        if (!isAlive())
        {
            lerpX = getX();
            lerpZ = getZ();

            if (lerpY > getY())
            {
                lerpY = getY();
            }
            lerpSteps = 3;
        }
    }

    @Inject(method = "lerpHeadTo", at = @At("TAIL"))
    private void onPosLerpHead(
        final float p_21005_, final int p_21006_, final CallbackInfo ci)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
        {
            return;
        }

        lerpHeadSteps = defaultLerp;
        if (level() instanceof ClientLevelDeltaTime deltaLevel)
        {
            lerpHeadSteps = (int) Math.max(3, Math.min(30, Math.round(defaultLerp * deltaLevel.getSlownessFactor())));
        }

        if (!isAlive())
        {
            lerpHeadSteps = 3;
        }
    }
}
