package com.smoothmovement;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SmoothMovement.MODID)
public class SmoothMovement
{
    public static final String MODID  = "smoothmovement";
    public static final Logger LOGGER = LogManager.getLogger();
    public static       Random rand   = new Random();

    public SmoothMovement(IEventBus modEventBus, ModContainer modContainer)
    {
        Compat.hourglass = FMLLoader.getLoadingModList().getModFileById("hourglass") != null || FMLLoader.getLoadingModList().getModFileById("betterdays") != null;
        Compat.genesis = FMLLoader.getLoadingModList().getModFileById("genesis") != null;
        NeoForge.EVENT_BUS.addListener(this::commandRegister);
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(new Command().build(event.getBuildContext()));
    }

    public static double getDistanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2)
    {
        final double xDiff = x1 - x2;
        final double yDiff = y1 - y2;
        final double zDiff = z1 - z2;

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }
}
