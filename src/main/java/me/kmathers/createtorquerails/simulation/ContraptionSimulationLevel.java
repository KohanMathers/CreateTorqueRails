package me.kmathers.createtorquerails.simulation;

import com.simibubi.create.content.contraptions.Contraption;
import net.createmod.catnip.levelWrappers.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ContraptionSimulationLevel extends WrappedLevel {

    private final ContraptionSimulation simulation;
    private final Contraption contraption;
    final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
    @Nullable
    private EmptyLevelChunk emptyChunk;

    public ContraptionSimulationLevel(Level level, ContraptionSimulation simulation, Contraption contraption) {
        super(level);
        this.simulation = simulation;
        this.contraption = contraption;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        StructureBlockInfo info = contraption.getBlocks().get(pos);
        return info == null ? Blocks.AIR.defaultBlockState() : info.state();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
        return predicate.test(getBlockState(pos));
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return blockEntities.get(pos);
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return false;
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return false;
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean nonnull) {
        if (emptyChunk == null) emptyChunk = new EmptyLevelChunk(this, ChunkPos.ZERO, registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
        return emptyChunk;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags) {
        return simulation.changeBlockState(pos, state);
    }

    @Override
    public void blockEntityChanged(BlockPos pos) {
        simulation.markChanged(pos);
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {}

    @Override
    public void removeBlockEntity(BlockPos pos) {}

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {}

    @Override
    public boolean addFreshEntity(Entity entity) {
        return simulation.spawnInWorld(entity);
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

    @Override
    public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {}

    @Override
    public void playSound(@Nullable Player player, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {}

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void gameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {}

    @Override
    public void gameEvent(GameEvent event, Vec3 pos, GameEvent.Context context) {}
}
