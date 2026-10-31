package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClientEntityMovementMixin extends Entity
{
    @Shadow
    protected int lerpHeadSteps;

    public ClientEntityMovementMixin(final EntityType<?> type, final Level level)
    {
        super(type, level);
    }

    @Inject(method = "lerpHeadTo", at = @At("TAIL"))
    private void onPosLerpHead(final float yRot, final int steps, final CallbackInfo ci)
    {
        if (!CommonConfiguration.config.getCommonConfig().enableLivingEntitySmoothing)
        {
            return;
        }

        lerpHeadSteps = steps;
        if (level() instanceof ClientLevelDeltaTime deltaLevel)
        {
            lerpHeadSteps = Math.clamp(Math.round(steps * deltaLevel.getSlownessFactor()), steps, steps * 10);
        }

        if (!isAlive())
        {
            lerpHeadSteps = 3;
        }
    }
}
