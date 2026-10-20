package me.kmathers.createtorquerails;

import com.mojang.logging.LogUtils;
import me.kmathers.createtorquerails.assembly.TrainFluidCapture;
import me.kmathers.createtorquerails.compat.createaddition.CreateAdditionCompat;
import me.kmathers.createtorquerails.network.TorqueRailsNetwork;
import me.kmathers.createtorquerails.simulation.ContraptionSimulationManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateTorqueRails.MODID)
public class CreateTorqueRails
{
    public static final String MODID = "createtorquerails";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateTorqueRails(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        TorqueRailsBlocks.register(modEventBus);
        context.registerConfig(ModConfig.Type.COMMON, TorqueRailsConfig.SPEC);
        TorqueRailsNetwork.register();

        MinecraftForge.EVENT_BUS.register(ContraptionSimulationManager.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        if (ModList.get().isLoaded("createaddition")) CreateAdditionCompat.register();
        LOGGER.info("CreateTorqueRails common setup");
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        TrainFluidCapture.register();
    }
}
