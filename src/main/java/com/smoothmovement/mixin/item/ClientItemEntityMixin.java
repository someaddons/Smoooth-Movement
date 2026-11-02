package com.smoothmovement.mixin.item;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.interpolation.ItemInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ClientItemEntityMixin extends Entity
{
    @Unique
    private ItemInterpolationHandler itemInterpolation;

    public ClientItemEntityMixin(final EntityType<?> type, final Level level)
    {
        super(type, level);
    }

    @Override
    public InterpolationHandler getInterpolation()
    {
        if (!level().isClientSide() || !CommonConfiguration.config.getCommonConfig().enableItemSmoothing)
        {
            if (itemInterpolation != null && itemInterpolation.hasActiveInterpolation())
            {
                itemInterpolation.cancel();
            }
            return super.getInterpolation();
        }

        if (itemInterpolation == null)
        {
            itemInterpolation = new ItemInterpolationHandler((ItemEntity) (Object) this);
        }
        return itemInterpolation;
    }

    @Override
    public void lerpMotion(final Vec3 movement)
    {
        if (getInterpolation() instanceof ItemInterpolationHandler interpolation)
        {
            interpolation.lerpMotion(movement);
        }
        else
        {
            super.lerpMotion(movement);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkUpdatePos(final CallbackInfo ci)
    {
        if (getInterpolation() instanceof ItemInterpolationHandler interpolation)
        {
            interpolation.interpolate();
        }
    }
}
