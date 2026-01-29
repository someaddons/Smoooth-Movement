package com.smoothmovement;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.smoothmovement.time.ServerTime;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class Command
{
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext)
    {
        return Commands.literal(SmoothMovement.MODID)
            .then(
                Commands.literal("setArtificialTPS")
                    .requires(stack -> stack.hasPermission(2))
                    .then(Commands.argument("tps", IntegerArgumentType.integer())
                        .executes(context ->
                        {
                            if (ServerTime.artificialLagTPS == -1)
                            {
                                ServerTime.artificialLagBaseMS = Math.max(0, 50 - (int) context.getSource().getLevel().getServer().getAverageTickTime());
                            }

                            ServerTime.artificialLagTPS = IntegerArgumentType.getInteger(context, "tps");

                            context.getSource()
                                .sendSystemMessage(Component.literal("Enable artifical lag for TPS:" + ServerTime.artificialLagTPS)
                                    .withStyle(ChatFormatting.RED));

                            return 1;
                        })
                    .then(
                        Commands.literal("stopArtificialLag")
                            .requires(stack -> stack.hasPermission(2))
                            .executes(context ->
                            {
                                ServerTime.artificialLagTPS = -1;

                                context.getSource()
                                    .sendSystemMessage(Component.literal("Disabled artifical lag")
                                        .withStyle(ChatFormatting.GREEN));

                                return 1;
                            })
                    )));
    }
}
