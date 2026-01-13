package com.smoothmovement.mixin.server;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

/**
 * 1.20.1 only! Mojang fixed it in 1.21+
 */
@Mixin(ServerEntity.class)
public class EntityPosSyncFix
{
    @Shadow @Final private VecDeltaCodec positionCodec;

    @Inject(method = "sendPairingData", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(F)I"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void fixPacketPos(
        final ServerPlayer p_289562_, final Consumer<Packet<ClientGamePacketListener>> p_289563_, final CallbackInfo ci, Packet<ClientGamePacketListener> packet)
    {
        if (packet instanceof ClientboundAddEntityPacket clientboundAddEntityPacket)
        {
            clientboundAddEntityPacket.x = positionCodec.base.x;
            clientboundAddEntityPacket.y = positionCodec.base.y;
            clientboundAddEntityPacket.z = positionCodec.base.z;
        }
    }
}
