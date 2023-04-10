package com.smoothmovement.mixin;

import com.smoothmovement.ClientLevelDeltaTime;
import com.smoothmovement.LivingEntityLerp;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MInecraftMixin
{
    @Shadow
    @Nullable
    public        ClientLevel level;
    private final Timer       timer60 = new Timer(60.0F, 0L);

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"))
    private void onInit(final boolean p_91384_, final CallbackInfo ci)
    {
        if (level != null)
        {
            final int ticks = timer60.advanceTime(Util.getMillis());

            for (int k = 0; k < Math.max(0, ticks); ++k)
            {
                ((ClientLevelDeltaTime) level).getTickingEntities().forEach(
                  entity -> {
                      if (entity instanceof LivingEntityLerp)
                      {
                          ((LivingEntityLerp) entity).doLerp();
                      }
                  });
            }
        }
    }
}
