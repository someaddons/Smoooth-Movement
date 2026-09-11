package com.smoothmovement.mixin.time;

import com.smoothmovement.Compat;
import com.smoothmovement.SmoothMovement;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelDayTimeMixin extends Level
{
    protected ServerLevelDayTimeMixin(
        final WritableLevelData p_270739_,
        final ResourceKey<Level> p_270683_,
        final RegistryAccess p_270200_,
        final Holder<DimensionType> p_270240_,
        final Supplier<ProfilerFiller> p_270692_,
        final boolean p_270904_,
        final boolean p_270470_,
        final long p_270248_,
        final int p_270466_)
    {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Shadow
    public abstract void setDayTime(final long p_8616_);

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V", shift = At.Shift.AFTER))
    private void adjustDayTime(final CallbackInfo ci)
    {
        if (ServerTime.extraTicks > 0 && CommonConfiguration.config.getCommonConfig().enableSkyLagAdjustedMovement && !Compat.hourglass && !Compat.genesis)
        {
            setDayTime(levelData.getDayTime() + ServerTime.extraTicks);
        }
    }
}
