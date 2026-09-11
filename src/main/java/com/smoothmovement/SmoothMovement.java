package com.smoothmovement;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
public class SmoothMovement implements ModInitializer
{
    public static final String MODID  = "smoothmovement";
    public static final Logger LOGGER = LogManager.getLogger();
    public static       Random rand   = new Random();

    public SmoothMovement()
    {
        Compat.hourglass = FabricLoader.getInstance().isModLoaded("hourglass") || FabricLoader.getInstance().isModLoaded("betterdays");
        Compat.genesis = FabricLoader.getInstance().isModLoaded("genesis");
    }

    public static double getDistanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2)
    {
        final double xDiff = x1 - x2;
        final double yDiff = y1 - y2;
        final double zDiff = z1 - z2;

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, c) ->
        {
            dispatcher.register(new Command().build(dedicated));
        });
    }
}
