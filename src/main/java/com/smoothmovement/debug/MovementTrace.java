package com.smoothmovement.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.smoothmovement.SmoothMovement;
import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ClientLevelDeltaTime;
import com.smoothmovement.time.ServerTime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Opt-in tick traces for normally ticking, non-passenger entities in development runs. */
public final class MovementTrace
{
    public static final int DEFAULT_SECONDS = 20;
    public static final int MAX_SECONDS = 120;

    // Both logical sides share this selection in an integrated server run.
    private static final AtomicReference<Capture> capture = new AtomicReference<>();

    private MovementTrace()
    {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command()
    {
        return Commands.literal("traceMovement")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("stop").executes(context -> stop(context.getSource())))
            .then(Commands.argument("entity", EntityArgument.entity())
                .executes(context -> start(context.getSource(), EntityArgument.getEntity(context, "entity").getUUID(), DEFAULT_SECONDS))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, MAX_SECONDS))
                    .executes(context -> start(context.getSource(), EntityArgument.getEntity(context, "entity").getUUID(),
                        IntegerArgumentType.getInteger(context, "seconds")))));
    }

    public static int start(final CommandSourceStack source, final UUID entity, final int seconds)
    {
        capture.set(new Capture(entity, System.nanoTime(), seconds));
        SmoothMovement.LOGGER.info("[movement_trace] start uuid={} seconds={}", entity, seconds);
        source.sendSystemMessage(Component.literal("Tracing movement for " + entity + " for " + seconds + " seconds in [movement_trace] log entries."));
        return 1;
    }

    public static int stop(final CommandSourceStack source)
    {
        final Capture stopped = capture.getAndSet(null);
        if (stopped == null)
        {
            source.sendSystemMessage(Component.literal("No movement trace is running."));
            return 0;
        }

        SmoothMovement.LOGGER.info("[movement_trace] stop uuid={}", stopped.entity());
        source.sendSystemMessage(Component.literal("Movement trace stopped."));
        return 1;
    }

    public static void beforeEntityTick(final EntityTickEvent.Pre event)
    {
        record(event.getEntity(), "pre");
    }

    public static void afterEntityTick(final EntityTickEvent.Post event)
    {
        record(event.getEntity(), "post");
    }

    private static void record(final Entity entity, final String phase)
    {
        final Capture current = capture.get();
        if (current == null)
        {
            return;
        }

        final long elapsedNanos = System.nanoTime() - current.startedAt();
        if (elapsedNanos >= current.seconds() * 1_000_000_000L)
        {
            if (capture.compareAndSet(current, null))
            {
                SmoothMovement.LOGGER.info("[movement_trace] expired uuid={}", current.entity());
            }
            return;
        }
        if (!entity.getUUID().equals(current.entity()))
        {
            return;
        }

        final boolean client = entity.level().isClientSide();
        final var config = CommonConfiguration.config.getCommonConfig();
        final var target = entity.getInterpolation().target();
        final JsonObject sample = new JsonObject();
        sample.addProperty("side", client ? "client" : "server");
        sample.addProperty("phase", phase);
        sample.addProperty("timeMs", System.currentTimeMillis());
        sample.addProperty("elapsedMs", elapsedNanos / 1_000_000.0);
        sample.addProperty("uuid", entity.getUUID().toString());
        sample.addProperty("entityId", entity.getId());
        sample.addProperty("type", entity.typeHolder().getRegisteredName());
        sample.addProperty("dimension", entity.level().dimension().identifier().toString());
        // Tick counters are local to each side; use UUID and time to compare the traces.
        sample.addProperty("entityTick", entity.tickCount);
        sample.addProperty("gameTime", entity.level().getGameTime());
        sample.add("position", vector(entity.position()));
        sample.add("deltaMovement", vector(entity.getDeltaMovement()));
        if (phase.equals("post"))
        {
            // Includes client interpolation, which does not necessarily match deltaMovement.
            sample.add("actualMovement", vector(entity.position().subtract(entity.oldPosition())));
        }
        sample.addProperty("onGround", entity.onGround());
        sample.addProperty("horizontalCollision", entity.horizontalCollision);
        sample.addProperty("verticalCollision", entity.verticalCollision);
        sample.addProperty("interpolating", entity.isInterpolating());
        sample.add("target", target == null ? JsonNull.INSTANCE : vector(target.position()));
        sample.addProperty("lagFactor", client
            ? entity.level() instanceof ClientLevelDeltaTime deltaLevel ? deltaLevel.getSlownessFactor() : 1.0
            : ServerTime.slownessFactor);
        sample.addProperty("livingServerEnabled", config.enableLivingEntityLagAdjustedMovement);
        sample.addProperty("livingClientEnabled", config.enableLivingEntitySmoothing);
        SmoothMovement.LOGGER.info("[movement_trace] {}", sample);
    }

    private static JsonArray vector(final Vec3 vector)
    {
        final JsonArray result = new JsonArray();
        result.add(vector.x);
        result.add(vector.y);
        result.add(vector.z);
        return result;
    }

    private record Capture(UUID entity, long startedAt, int seconds)
    {
    }
}
