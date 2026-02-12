package com.smoothmovement.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ServerItemEntityMixin extends Entity
{
    public ServerItemEntityMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private Vec3 changeVel(final Vec3 org)
    {
        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableItemLagAdjustedMovement)
        {
            return org;
        }

        return org.scale(ServerTime.slownessFactor);
    }

    @ModifyReturnValue(method = "getDefaultGravity", at = @At("RETURN"))
    private double changeGravity(final double gravity)
    {
        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableItemLagAdjustedMovement)
        {
            return gravity;
        }

        return gravity * ServerTime.slownessFactor;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 0), index = 0)
    private double changeVelX(final double x)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableItemLagAdjustedMovement)
        {
            return x;
        }

        return Math.pow(x, ServerTime.slownessFactor * 0.45);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 0), index = 1)
    private double changeVelY(final double y)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableItemLagAdjustedMovement)
        {
            return y;
        }

        return Math.pow(y, ServerTime.slownessFactor * 0.45);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 0), index = 2)
    private double changeVelZ(final double z)
    {
        if (level().isClientSide || !ServerTime.hasLag() || !CommonConfiguration.config.getCommonConfig().enableItemLagAdjustedMovement)
        {
            return z;
        }

        return Math.pow(z, ServerTime.slownessFactor * 0.45);
    }
}
