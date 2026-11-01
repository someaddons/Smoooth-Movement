package com.smoothmovement.debug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.smoothmovement.SmoothMovement;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** Local control when the development client and server run in separate JVMs. */
@EventBusSubscriber(modid = SmoothMovement.MODID, value = Dist.CLIENT)
public final class ClientMovementTrace
{
    private ClientMovementTrace()
    {
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterClientCommandsEvent event)
    {
        if (FMLEnvironment.isProduction())
        {
            return;
        }

        event.getDispatcher().register(Commands.literal("smoothmovementTrace")
            .then(Commands.literal("stop").executes(context -> MovementTrace.stop(context.getSource())))
            .then(Commands.argument("entity", UuidArgument.uuid())
                .executes(context -> MovementTrace.start(context.getSource(), UuidArgument.getUuid(context, "entity"), MovementTrace.DEFAULT_SECONDS))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, MovementTrace.MAX_SECONDS))
                    .executes(context -> MovementTrace.start(context.getSource(), UuidArgument.getUuid(context, "entity"),
                        IntegerArgumentType.getInteger(context, "seconds"))))));
    }
}
