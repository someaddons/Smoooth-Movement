package com.smoothmovement.mixin;

import com.smoothmovement.ClientLevelDeltaTime;
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
public abstract class LivingEntityMixin extends Entity
{
    @Shadow
    protected int lerpSteps;
    @Shadow
    protected int lerpHeadSteps;
    @Unique
    private   int ticksSinceLastUpdate     = 0;
    @Unique
    private   int ticksSinceLastUpdateHead = 0;

    public LivingEntityMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(final CallbackInfo ci)
    {
        ticksSinceLastUpdate++;
        ticksSinceLastUpdateHead++;
    }

    @Inject(method = "lerpTo", at = @At("TAIL"))
    private void onPosLerp(
      final double p_20977_,
      final double p_20978_,
      final double p_20979_,
      final float p_20980_,
      final float p_20981_,
      final int steps,
      final boolean p_20983_, final CallbackInfo ci)
    {
        if (ticksSinceLastUpdate != 0)
        {
            lerpSteps = Math.max(steps, Math.min(ticksSinceLastUpdate, ((ClientLevelDeltaTime) this.level).getDeltaTime()));
        }

        ticksSinceLastUpdate = 0;
    }

    @Inject(method = "lerpHeadTo", at = @At("TAIL"))
    private void onPosLerpHead(
      final float p_21005_, final int p_21006_, final CallbackInfo ci)
    {
        if (ticksSinceLastUpdateHead != 0)
        {
            lerpHeadSteps = Math.max(lerpHeadSteps, Math.min(ticksSinceLastUpdateHead, ((ClientLevelDeltaTime) this.level).getDeltaTime()));
        }

        ticksSinceLastUpdateHead = 0;
    }
}
