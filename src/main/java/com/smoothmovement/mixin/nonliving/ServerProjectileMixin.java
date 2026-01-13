package com.smoothmovement.mixin.nonliving;

import com.smoothmovement.SmoothMovement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ServerProjectileMixin extends Entity
{
    @Shadow public abstract void tick();

    public ServerProjectileMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Unique
    private boolean extraTicking = false;

    @Inject(method = "tick", at = @At("RETURN"))
    private void extraTicks(final CallbackInfo ci)
    {
        if (!extraTicking && !level().isClientSide)
        {
            extraTicking = true;
            for (int i = 0; i < SmoothMovement.extraTicks; i++)
            {
                tick();
            }
            extraTicking = false;
        }
    }
}
