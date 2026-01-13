package com.smoothmovement;

import com.smoothmovement.config.Configuration;
import com.smoothmovement.event.ClientEventHandler;
import com.smoothmovement.event.EventHandler;
import com.smoothmovement.event.ModEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SmoothMovement.MODID)
public class SmoothMovement
{
    public static final String        MODID  = "smoothmovement";
    public static final Logger        LOGGER = LogManager.getLogger();
    //public static       Configuration config = new Configuration();
    public static       Random        rand   = new Random();
    public static float slownessFactor = 1.0f;
    public static int extraTicks = 0;
    public static double extraTickTotal = 0;

    public SmoothMovement()
    {
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getCommonConfig().ForgeConfigSpecBuilder);
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ModEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public static boolean lag = true;

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info(MODID + " mod initialized");
    }

    public static double getDistanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2)
    {
        final double xDiff = x1-x2;
        final double yDiff = y1-y2;
        final double zDiff = z1-z2;

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }
}
