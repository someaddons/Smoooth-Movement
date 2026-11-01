package com.smoothmovement.mixin.item;

import com.smoothmovement.interpolation.ItemInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemEntity.class)
public abstract class ClientItemEntityMixin extends Entity
{
    public ClientItemEntityMixin(final EntityType<?> type, final Level level)
    {
        super(type, level);
    }

    @Override
    protected InterpolationHandler createInterpolationHandler()
    {
        return level().isClientSide() ? new ItemInterpolationHandler((ItemEntity) (Object) this) : super.createInterpolationHandler();
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
}
