package com.smoothmovement.mixin.time;

import com.smoothmovement.SmoothMovement;
import com.smoothmovement.time.ClientLevelDeltaTime;
import com.smoothmovement.time.ServerTime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.clock.ClockNetworkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class GametimePacketListenerMixin
{
    @Shadow private ClientLevel level;

    @Inject(method = "handleSetTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setTimeFromServer(J)V"))
    private void onSetGameTime(final ClientboundSetTimePacket packet, final CallbackInfo ci)
    {
        final ClockNetworkState state = level.dimensionType().defaultClock().map(packet.clockUpdates()::get).orElse(null);
        ((ClientLevelDeltaTime) level).onTimePacket(packet.gameTime(), state == null ? -1 : state.totalTicks(), state == null ? 0 : state.rate());
    }

    @Inject(method = "handleSetScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetScorePacket;objectiveName()Ljava/lang/String;"), cancellable = true)
    private void onSetGameTime(final ClientboundSetScorePacket packet, final CallbackInfo ci)
    {
        if (packet.owner().equals(SmoothMovement.MODID) && ServerTime.SCOREBOARD_TPS.equals(packet.objectiveName())
            && level instanceof ClientLevelDeltaTime clientLevelDeltaTime)
        {
            clientLevelDeltaTime.onScoreBoardPacket(packet.score());
            ci.cancel();
        }
    }
}
