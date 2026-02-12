package com.smoothmovement.mixin.nonliving;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FallingBlockEntity.class)
public abstract class ServerFallingBlockMixin extends Entity
{
    public ServerFallingBlockMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private Vec3 changeVel(final Vec3 org)
    {
        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableFallingBlockLagAdjustedMovement)
        {
            return org;
        }

        return org.scale(ServerTime.slownessFactor);
    }

    @ModifyReturnValue(method = "getDefaultGravity", at = @At("RETURN"))
    private double changeGravity(final double gravity)
    {
        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableFallingBlockLagAdjustedMovement)
        {
            return gravity;
        }

        return gravity * ServerTime.slownessFactor;
    }
}
