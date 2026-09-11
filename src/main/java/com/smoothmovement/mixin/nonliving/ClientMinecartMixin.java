package com.smoothmovement.mixin.nonliving;

import com.mojang.datafixers.util.Pair;
import com.smoothmovement.SmoothMovement;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import com.smoothmovement.time.ServerTime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractMinecart.class)
public abstract class ClientMinecartMixin extends Entity implements IForgeAbstractMinecart
{
    @Shadow
    private int lSteps;

    @Shadow
    private double lx;

    @Shadow
    private double lz;

    @Shadow
    private double ly;

    @Shadow
    private boolean onRails;

    @Unique
    private int velocityLerpSteps = 3;

    @Shadow
    protected abstract void moveAlongTrack(final BlockPos p_38156_, final BlockState p_38157_);

    @Shadow
    @Final
    private static Map<RailShape, Pair<Vec3i, Vec3i>> EXITS;

    public ClientMinecartMixin(final EntityType<?> p_19870_, final Level p_19871_)
    {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "lerpTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;setDeltaMovement(DDD)V"), cancellable = true)
    private void onPosLerp(
        final double x,
        final double y,
        final double z,
        final float p_20980_,
        final float p_20981_,
        final int steps,
        final boolean teleport,
        final CallbackInfo ci)
    {
        if (!(level() instanceof ClientLevelDeltaTime deltaLevel) || deltaLevel.getSlownessFactor() <= 1.0 || !CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing || !onRails)
        {
            return;
        }

        lSteps = 0;
        if (teleport || SmoothMovement.getDistanceSquared(x, y, z, getX(), getY(), getZ()) > 10 * 10)
        {
            setPos(x, y, z);
            return;
        }

        //level().addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);

        if (level().getBlockState(BlockPos.containing(x, y, z)).is(BlockTags.RAILS))
        {
            lSteps = (int) Math.max(5, Math.min(100, Math.round(5 * (deltaLevel.getSlownessFactor())))) * 2;
        }
        velocityLerpSteps = (int) Math.max(3, Math.min(30, Math.round(5 * (deltaLevel.getSlownessFactor()))));

        if (!isAlive())
        {
            lx = getX();
            lz = getZ();

            if (ly > getY())
            {
                ly = getY();
            }
        }
        ci.cancel();
    }

    @Inject(method = "lerpMotion", at = @At("HEAD"), cancellable = true)
    public void lerpMotion(final double x, final double y, final double z, final CallbackInfo ci)
    {
        if (!(level() instanceof ClientLevelDeltaTime deltaLevel) || deltaLevel.getSlownessFactor() <= 1.0 || !CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing || !onRails)
        {
            return;
        }

        ci.cancel();

        if (getDeltaMovement().length() <= 0.01)
        {
            setDeltaMovement(x, y, z);
            return;
        }

        if (x == 0 && y == 0 && z == 0)
        {
            setDeltaMovement(0, 0, 0);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(final CallbackInfo ci)
    {
        if (!level().isClientSide || !CommonConfiguration.config.getCommonConfig().enableMinecartSmoothing)
        {
            return;
        }

        if (!(level() instanceof ClientLevelDeltaTime deltaLevel) || deltaLevel.getSlownessFactor() <= 1.0)
        {
            return;
        }

        Vec3 diff = position().subtract(new Vec3(lx, ly, lz)).reverse().multiply(1.0 / velocityLerpSteps, 1.0 / velocityLerpSteps, 1.0 / velocityLerpSteps);

        if (diff.x < 0 && getDeltaMovement().x >= 0
            || diff.x > 0 && getDeltaMovement().x <= 0)
        {
            diff = new Vec3(0, diff.y, diff.z);
        }

        if (diff.y < 0 && getDeltaMovement().y >= 0
            || diff.y > 0 && getDeltaMovement().y <= 0)
        {
            diff = new Vec3(diff.x, 0, diff.z);
        }

        if (diff.z < 0 && getDeltaMovement().z >= 0
            || diff.z > 0 && getDeltaMovement().z <= 0)
        {
            diff = new Vec3(diff.x, diff.y, 0);
        }

        setDeltaMovement(new Vec3((diff.x()) * 0.3 + getDeltaMovement().x() * 0.7,
            (diff.y()) * 0.3 + getDeltaMovement().y() * 0.7,
            (diff.z()) * 0.3 + getDeltaMovement().z() * 0.7));

        if (!this.isNoGravity())
        {
            double d0 = this.isInWater() ? -0.005D : -0.04D;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d0, 0.0D));
        }

        int k = Mth.floor(this.getX());
        int i = Mth.floor(this.getY());
        int j = Mth.floor(this.getZ());
        if (this.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS))
        {
            --i;
        }

        BlockPos blockpos = new BlockPos(k, i, j);
        BlockState blockstate = this.level().getBlockState(blockpos);
        this.onRails = BaseRailBlock.isRail(blockstate);
        if (canUseRail() && this.onRails)
        {
            this.moveAlongTrack(blockpos, blockstate);
        }
        this.updateInWaterStateAndDoFluidPushing();
    }
}
