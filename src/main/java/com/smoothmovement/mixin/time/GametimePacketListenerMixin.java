package com.smoothmovement.mixin.time;

import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class GametimePacketListenerMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleSetTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setGameTime(J)V"))
    private void onSetGameTime(final ClientboundSetTimePacket packet, final CallbackInfo ci)
    {
        ((ClientLevelDeltaTime) minecraft.level).onTimePacket(packet.getGameTime(), packet.getDayTime());
    }
}
