package me.kmathers.createtorquerails.client;

import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import me.kmathers.createtorquerails.CreateTorqueRails;
import me.kmathers.createtorquerails.TorqueRailsBlocks;
import me.kmathers.createtorquerails.client.ponder.TorqueRailsPonderPlugin;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CreateTorqueRails.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TorqueRailsClient {

    private TorqueRailsClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerTooltip(TorqueRailsBlocks.TRAIN_ENGINE_ITEM.get());
        registerTooltip(TorqueRailsBlocks.CREATIVE_TRAIN_ENGINE_ITEM.get());
        event.enqueueWork(() -> SimpleBlockEntityVisualizer.builder(TorqueRailsBlocks.TRAIN_ENGINE_BLOCK_ENTITY.get()).factory(ShaftVisual::new).skipVanillaRender(be -> true).apply());
        event.enqueueWork(() -> PonderIndex.addPlugin(new TorqueRailsPonderPlugin()));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TorqueRailsBlocks.TRAIN_ENGINE_BLOCK_ENTITY.get(), ShaftRenderer::new);
    }

    private static void registerTooltip(Item item) {
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }
}
