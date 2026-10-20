package me.kmathers.createtorquerails;

import me.kmathers.createtorquerails.engine.CreativeTrainEngineBlock;
import me.kmathers.createtorquerails.engine.TrainEngineBlock;
import me.kmathers.createtorquerails.engine.TrainEngineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static me.kmathers.createtorquerails.CreateTorqueRails.MODID;

public final class TorqueRailsBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<TrainEngineBlock> TRAIN_ENGINE = BLOCKS.register("train_engine", () -> new TrainEngineBlock(engineProperties()));
    public static final RegistryObject<CreativeTrainEngineBlock> CREATIVE_TRAIN_ENGINE = BLOCKS.register("creative_train_engine", () -> new CreativeTrainEngineBlock(engineProperties()));

    public static final RegistryObject<Item> TRAIN_ENGINE_ITEM = ITEMS.register("train_engine", () -> new BlockItem(TRAIN_ENGINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> CREATIVE_TRAIN_ENGINE_ITEM = ITEMS.register("creative_train_engine", () -> new BlockItem(CREATIVE_TRAIN_ENGINE.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<TrainEngineBlockEntity>> TRAIN_ENGINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("train_engine", () -> BlockEntityType.Builder.of((pos, state) -> new TrainEngineBlockEntity(TorqueRailsBlocks.TRAIN_ENGINE_BLOCK_ENTITY.get(), pos, state), TRAIN_ENGINE.get()).build(null));

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + MODID)).icon(() -> new ItemStack(TRAIN_ENGINE_ITEM.get())).displayItems((parameters, output) -> {
        output.accept(TRAIN_ENGINE_ITEM.get());
        output.accept(CREATIVE_TRAIN_ENGINE_ITEM.get());
    }).build());

    private TorqueRailsBlocks() {}

    private static BlockBehaviour.Properties engineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion();
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
