package com.smoothmovement.mixin;

import com.smoothmovement.LivingEntityLerp;
import net.minecraft.util.Mth;
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
public abstract class LivingEntityMixin extends Entity implements LivingEntityLerp
{
    @Shadow
    protected int lerpSteps;
    @Shadow protected int lerpHeadSteps;
    @Shadow protected double lerpX;
    @Shadow protected double lerpY;
    @Shadow protected double lerpZ;
    @Shadow protected double lerpYRot;
    @Shadow protected double lerpXRot;
    @Shadow public float yHeadRot;
    @Shadow protected double lyHeadRot;
    @Unique
    private   int ticksSinceLastUpdate = 0;
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

    @Unique
    private int customLerpSteps = 0;

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
        lerpSteps *= 3;
        customLerpSteps = lerpSteps;
        lerpSteps = 0;
        if (ticksSinceLastUpdate != 0)
        {
           // lerpSteps = Math.max(steps, Math.min(ticksSinceLastUpdate, ((ClientLevelDeltaTime) this.level).getDeltaTime()));
        }

        ticksSinceLastUpdate = 0;
    }


    @Unique
    private int customLerpHeadSteps = 0;

    @Inject(method = "lerpHeadTo", at = @At("TAIL"))
    private void onPosLerpHead(
      final float p_21005_, final int p_21006_, final CallbackInfo ci)
    {
        if (ticksSinceLastUpdateHead != 0)
        {
           // lerpHeadSteps = Math.max(lerpHeadSteps, Math.min(ticksSinceLastUpdateHead, ((ClientLevelDeltaTime) this.level).getDeltaTime()));
        }

        lerpHeadSteps *= 3;
        customLerpHeadSteps = lerpHeadSteps;
        lerpHeadSteps = 0;
        ticksSinceLastUpdateHead = 0;
    }

    @Override
    public void doLerp()
    {
        if (this.customLerpSteps > 0) {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.customLerpSteps;
            double d2 = this.getY() + (this.lerpY - this.getY()) / (double)this.customLerpSteps;
            double d4 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.customLerpSteps;
            double d6 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float)d6 / (float)this.customLerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.customLerpSteps);
            --this.customLerpSteps;
            this.setPos(d0, d2, d4);
            this.setRot(this.getYRot(), this.getXRot());
        }

        if (this.customLerpHeadSteps > 0) {
            this.yHeadRot += (float)Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (float)this.customLerpHeadSteps;
            --this.customLerpHeadSteps;
        }
    }
}
