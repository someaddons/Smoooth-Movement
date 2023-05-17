package com.smoothmovement.mixin;

import com.smoothmovement.SmoothMovement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityServerMixin extends Entity
{
    public LivingEntityServerMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "getSpeed", at = @At(value = "RETURN"), cancellable = true)
    private void smoothmovement$getSpeed(final CallbackInfoReturnable<Float> cir)
    {
        cir.setReturnValue(cir.getReturnValue() * SmoothMovement.slownessFactor);
    }
}
