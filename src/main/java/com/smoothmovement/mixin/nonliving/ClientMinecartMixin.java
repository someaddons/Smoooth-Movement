package com.smoothmovement.mixin.nonliving;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Experimental minecarts support, since its simple
@Mixin(NewMinecartBehavior.class)
public abstract class ClientMinecartMixin extends MinecartBehavior
{
    @Unique
    private int interpolationSteps = 3;

    protected ClientMinecartMixin(final AbstractMinecart minecart)
    {
        super(minecart);
    }

    @ModifyConstant(method = "lerpClientPositionAndRotation", constant = @Constant(intValue = 3))
    private int adjustSteps(final int steps)
    {
        interpolationSteps = steps;
        if (level() instanceof ClientLevelDeltaTime deltaLevel && CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing)
        {
            interpolationSteps = Math.clamp(Math.round(steps * deltaLevel.getSlownessFactor()), steps, 100);
        }
        return interpolationSteps;
    }

    @ModifyConstant(method = "getCurrentLerpStep", constant = @Constant(intValue = 3))
    private int adjustElapsedSteps(final int steps)
    {
        return interpolationSteps;
    }

    @ModifyConstant(method = "getCurrentLerpStep", constant = @Constant(floatValue = 3.0F))
    private float adjustStepLength(final float steps)
    {
        return interpolationSteps;
    }
}
