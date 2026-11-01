package com.smoothmovement;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.smoothmovement.debug.MovementTrace;
import com.smoothmovement.time.ServerTime;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;

public class Command
{
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext)
    {
        final var command = Commands.literal(SmoothMovement.MODID)
            .then(
                Commands.literal("setArtificialTPS")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.argument("tps", IntegerArgumentType.integer())
                        .executes(context ->
                        {
                            if (ServerTime.artificialLagTPS == -1)
                            {
                                ServerTime.artificialLagBaseMS = Math.max(0, 50 - (int) context.getSource().getLevel().getServer().getAverageTickTimeNanos());
                            }

                            ServerTime.artificialLagTPS = IntegerArgumentType.getInteger(context, "tps");

                            context.getSource()
                                .sendSystemMessage(Component.literal("Enable artifical lag for TPS:" + ServerTime.artificialLagTPS)
                                    .withStyle(ChatFormatting.RED));

                            return 1;
                        })))
            .then(
                Commands.literal("stopArtificialLag")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context ->
                    {
                        ServerTime.artificialLagTPS = -1;

                        context.getSource()
                            .sendSystemMessage(Component.literal("Disabled artifical lag")
                                .withStyle(ChatFormatting.GREEN));

                        return 1;
                    })
            );

        if (!FMLEnvironment.isProduction())
        {
            command.then(MovementTrace.command());
        }
        return command;
    }
}
