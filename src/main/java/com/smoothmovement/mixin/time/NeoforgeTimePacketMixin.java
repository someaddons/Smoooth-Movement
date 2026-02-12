package com.smoothmovement.mixin.time;

import com.smoothmovement.time.ClientLevelDeltaTime;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPayloadHandler.class)
public class NeoforgeTimePacketMixin
{
    @Inject(method = "handle(Lnet/neoforged/neoforge/network/payload/ClientboundCustomSetTimePayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
        at = @At("HEAD"))
    private static void onSetGameTime(final ClientboundCustomSetTimePayload packet, final IPayloadContext context, final CallbackInfo ci)
    {
        ((ClientLevelDeltaTime) Minecraft.getInstance().level).onTimePacket(packet.gameTime(), packet.dayTime() < 0 ? -packet.dayTime() : packet.dayTime());
    }
}
