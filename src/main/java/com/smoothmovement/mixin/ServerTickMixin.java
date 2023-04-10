package com.smoothmovement.mixin;

import com.smoothmovement.SmoothMovement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class ServerTickMixin
{
    @Shadow @Final public long[] tickTimes;

    @Shadow public abstract int getTickCount();

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getNanos()J", shift = At.Shift.AFTER, ordinal = 0))
    private void onServerTick(final BooleanSupplier p_129871_, final CallbackInfo ci)
    {
        try
        {
           Thread.sleep(1);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        SmoothMovement.LOGGER.info("Servertick");

        final double lastTickMs = this.tickTimes[this.getTickCount() % 100] * 1.0E-6D;
        if (lastTickMs > 50)
        {
            SmoothMovement.slownessFactor = Mth.clamp(lastTickMs / 50, 1.0D, 5.0D);
        }
        else
        {
            SmoothMovement.slownessFactor = 1.0D;
        }
    }
}
