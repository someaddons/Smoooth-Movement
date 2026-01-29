package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class ServerLivingEntityMixin extends Entity
{
    public ServerLivingEntityMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @ModifyArg(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private Vec3 changeVel(final Vec3 org)
    {
        // || ((Object) this) instanceof Player
        if (level().isClientSide || org.lengthSqr() <= 1.0E-7D || !CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            return org;
        }

        return org.scale(ServerTime.slownessFactor);
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"), index = 2)
    private double onGravity(final double gravity)
    {
        // || ((Object) this) instanceof Player
        if (level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement)
        {
            return gravity;
        }

        return gravity * ServerTime.slownessFactor;
    }
}
