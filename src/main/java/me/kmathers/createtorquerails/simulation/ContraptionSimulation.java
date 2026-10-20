package me.kmathers.createtorquerails.simulation;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour;
import com.simibubi.create.content.contraptions.bearing.StabilizedBearingMovementBehaviour;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorMovementBehaviour;
import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.logistics.funnel.FunnelMovementBehaviour;
import com.simibubi.create.content.redstone.contact.ContactMovementBehaviour;
import me.kmathers.createtorquerails.TorqueRailsBlocks;
import me.kmathers.createtorquerails.TorqueRailsConfig;
import me.kmathers.createtorquerails.engine.TrainEngineBlockEntity;
import me.kmathers.createtorquerails.engine.TrainPower;
import me.kmathers.createtorquerails.mixin.ContraptionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContraptionSimulation {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int[] DEBUG_DUMP_TICKS = {1, 40};
    private static final int DEBUG_SUMMARY_INTERVAL = 400;
    private static final int MAX_SHAPE_UPDATE_DEPTH = 8;
    private static final int LOAD_UPDATE_INTERVAL = 20;

    private record Ticking<T extends BlockEntity>(T blockEntity, BlockEntityTicker<T> ticker) {
        void tick() {
            ticker.tick(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
        }
    }

    private record KineticState(float speed, boolean overStressed) {}

    private final String label;
    private final AbstractContraptionEntity entity;
    private final Contraption contraption;
    private final ContraptionSimulationLevel level;
    private final List<Ticking<?>> ticking = new ArrayList<>();
    private final List<KineticBlockEntity> kineticBlockEntities = new ArrayList<>();
    private final List<StorageBridge<?>> storageBridges = new ArrayList<>();
    private final Map<BlockPos, KineticState> lastSyncedKinetics = new HashMap<>();
    private final Set<BlockPos> changed = new HashSet<>();
    private final List<TrainEngineBlockEntity> engines = new ArrayList<>();
    private boolean creativeEngine;
    private float drive;
    private int ticks;
    private int storagePulls, storagePushes, blockStateChanges, deniedBlockChanges, writeBacks;

    public ContraptionSimulation(String label, AbstractContraptionEntity entity, Contraption contraption) {
        this.label = label;
        this.entity = entity;
        this.contraption = contraption;
        this.level = new ContraptionSimulationLevel(entity.level(), this, contraption);
        SimulationHooks.start(level);

        Map<String, Integer> fluidBlocks = new HashMap<>();
        for (StructureBlockInfo info : contraption.getBlocks().values()) {
            if (info.state().getBlock() instanceof LiquidBlock) fluidBlocks.merge(info.state().getBlock().toString(), 1, Integer::sum);
            if (info.state().is(TorqueRailsBlocks.CREATIVE_TRAIN_ENGINE.get())) creativeEngine = true;
        }
        LOGGER.debug("Contraption sim [{}] starting: {} blocks in contraption, fluid blocks: {}", label, contraption.getBlocks().size(), fluidBlocks);

        for (StructureBlockInfo info : contraption.getBlocks().values()) {
            BlockEntity be = createBlockEntity(info);
            if (be != null) add(be);
        }

        setUpSteamEngines();
        bridgeStorages();

        LOGGER.debug("Contraption sim [{}] started: {} block entities, {} ticked, {} kinetic, {} storages bridged, " + "{} train engines{}", label, level.blockEntities.size(), ticking.size(), kineticBlockEntities.size(), storageBridges.size(), engines.size(), creativeEngine ? " + creative train engine" : "");
    }

    public Contraption contraption() {
        return contraption;
    }

    private void add(BlockEntity be) {
        BlockPos pos = be.getBlockPos();
        level.blockEntities.put(pos, be);
        if (be instanceof KineticBlockEntity kbe) kineticBlockEntities.add(kbe);
        if (be instanceof TrainEngineBlockEntity engine) engines.add(engine);
        Ticking<?> tick = shouldTick(be.getBlockState()) ? tickerFor(be) : null;
        if (tick != null) ticking.add(tick);
        LOGGER.debug("Contraption sim [{}]   {} {} kinetic={} ticked={}{}", label, pos, be.getBlockState().getBlock(), be instanceof KineticBlockEntity, tick != null, be instanceof KineticBlockEntity kbe ? " generatedSpeed=" + generatedSpeedForLog(kbe) : "");
    }

    private void remove(BlockPos pos) {
        BlockEntity be = level.blockEntities.remove(pos);
        if (be == null) return;
        kineticBlockEntities.remove(be);
        engines.remove(be);
        ticking.removeIf(tick -> tick.blockEntity() == be);
    }

    private static boolean shouldTick(BlockState state) {
        MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(state);
        return behaviour == null || !actsOnWorldWhileMoving(behaviour);
    }

    private static boolean actsOnWorldWhileMoving(MovementBehaviour behaviour) {
        return behaviour instanceof BlockBreakingMovementBehaviour
                || behaviour instanceof HarvesterMovementBehaviour
                || behaviour instanceof DeployerMovementBehaviour
                || behaviour instanceof PortableStorageInterfaceMovement
                || behaviour instanceof ContraptionControlsMovement
                || behaviour instanceof ControlsMovementBehaviour
                || behaviour instanceof SeatMovementBehaviour
                || behaviour instanceof ContactMovementBehaviour
                || behaviour instanceof StabilizedBearingMovementBehaviour
                || behaviour instanceof SlidingDoorMovementBehaviour
                || behaviour instanceof FunnelMovementBehaviour;
    }

    @Nullable
    private BlockEntity createBlockEntity(StructureBlockInfo info) {
        BlockState state = info.state();
        if (!state.hasBlockEntity() || !(state.getBlock() instanceof EntityBlock entityBlock)) return null;
        try {
            BlockEntity be = entityBlock.newBlockEntity(info.pos(), state);
            if (be == null) return null;

            be.setLevel(level);
            if (info.nbt() != null) {
                CompoundTag nbt = info.nbt().copy();
                nbt.remove("LastKnownPos");
                be.load(nbt);
            }

            if (be instanceof KineticBlockEntity kbe) {
                kbe.clearKineticInformation();
                kbe.updateSpeed = true;
            }
            return be;
        } catch (Exception e) {
            LOGGER.error("Contraption sim [{}] couldn't create block entity for {} at {}, leaving it out", label, state, info.pos(), e);
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> Ticking<T> tickerFor(T be) {
        BlockEntityTicker<T> ticker = be.getBlockState().getTicker(level, (BlockEntityType<T>) be.getType());
        return ticker == null ? null : new Ticking<>(be, ticker);
    }

    private void setUpSteamEngines() {
        for (StructureBlockInfo info : List.copyOf(contraption.getBlocks().values())) {
            BlockState engineState = info.state();
            if (!AllBlocks.STEAM_ENGINE.has(engineState)) continue;
            BlockPos enginePos = info.pos();
            BlockPos shaftPos = SteamEngineBlock.getShaftPos(engineState, enginePos);
            StructureBlockInfo shaftInfo = contraption.getBlocks().get(shaftPos);
            BlockState shaftState = shaftInfo == null ? null : shaftInfo.state();

            String outcome;
            if (shaftState == null || !SteamEngineBlock.isShaftValid(engineState, shaftState)) {
                outcome = "no valid shaft on the contraption";
            } else if (AllBlocks.POWERED_SHAFT.has(shaftState)) {
                outcome = "already powered";
            } else {
                BlockState poweredState = PoweredShaftBlock.getEquivalent(shaftState);
                entity.setBlock(shaftPos, new StructureBlockInfo(shaftPos, poweredState, null));
                remove(shaftPos);
                BlockEntity poweredShaft = createBlockEntity(new StructureBlockInfo(shaftPos, poweredState, null));
                if (poweredShaft != null) add(poweredShaft);
                outcome = "powered";
            }

            FluidTankBlock.updateBoilerState(engineState, level, enginePos.relative(SteamEngineBlock.getFacing(engineState).getOpposite()));
            LOGGER.debug("Contraption sim [{}]   steam engine at {} -> shaft at {}: {}", label, enginePos, shaftPos, outcome);
        }
    }

    private void bridgeStorages() {
        MountedStorageManager storage = contraption.getStorage();
        storage.getFluids().storages.forEach((pos, fluidStorage) -> bridge(pos, fluidStorage, null));
        storage.getAllItemStorages().forEach((pos, itemStorage) -> bridge(pos, null, itemStorage));
    }

    private void bridge(BlockPos pos, @Nullable MountedFluidStorage fluids, @Nullable MountedItemStorage items) {
        BlockEntity be = level.blockEntities.get(pos);
        StructureBlockInfo info = contraption.getBlocks().get(pos);
        if (be == null || info == null) return;
        StorageBridge<?> bridge = fluids != null ? StorageBridge.Fluids.create(level, pos, info.state(), be, fluids) : StorageBridge.Items.create(level, pos, info.state(), be, items);
        if (bridge != null) storageBridges.add(bridge);
        LOGGER.debug("Contraption sim [{}]   {} storage at {} {}", label, fluids != null ? "fluid" : "item", pos, bridge != null ? "bridged: " + bridge.describe(level) : "not bridged (block entity doesn't mount as a matching storage)");
    }

    private static String generatedSpeedForLog(KineticBlockEntity be) {
        try {
            return String.valueOf(be.getGeneratedSpeed());
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    public void tick() {
        ticks++;

        for (StorageBridge<?> bridge : storageBridges) if (bridge.pull(level)) storagePulls++;

        Iterator<Ticking<?>> iterator = ticking.iterator();
        while (iterator.hasNext()) {
            Ticking<?> tick = iterator.next();
            try {
                tick.tick();
            } catch (Exception e) {
                LOGGER.error("Contraption sim [{}] tick of {} at {} failed, dropping it from the simulation", label, tick.blockEntity().getBlockState(), tick.blockEntity().getBlockPos(), e);
                iterator.remove();
            }
        }
        SimulationHooks.afterTick(level);
        reportTrainPower();

        for (StorageBridge<?> bridge : storageBridges) if (bridge.push(level)) storagePushes++;

        for (int dumpTick : DEBUG_DUMP_TICKS) if (ticks == dumpTick) dumpState();
        if (ticks % DEBUG_SUMMARY_INTERVAL == 0) logSummary();
    }

    public boolean useBlock(Player player, BlockPos localPos, Direction side, InteractionHand hand) {
        BlockState state = level.getBlockState(localPos);
        if (state.isAir()) return false;
        if (player.isSecondaryUseActive() && !(player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty())) return false;

        for (StorageBridge<?> bridge : storageBridges) if (bridge.pull(level)) storagePulls++;

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(localPos).relative(side, 0.5), side, localPos, false);
        InteractionResult result;
        try {
            result = state.use(level, player, hand, hit);
        } catch (Exception e) {
            LOGGER.error("Contraption sim [{}] using {} at {} failed", label, state, localPos, e);
            return false;
        }

        for (StorageBridge<?> bridge : storageBridges) if (bridge.push(level)) storagePushes++;
        LOGGER.debug("Contraption sim [{}] {} used {} at {}: {}", label, player.getName().getString(), state.getBlock(), localPos, result);
        return result.consumesAction();
    }

    boolean spawnInWorld(Entity spawned) {
        Level world = entity.level();
        Vec3 pos = entity.toGlobalVector(spawned.position(), 1);
        Entity real;
        if (spawned instanceof ItemEntity item) {
            ItemEntity copy = new ItemEntity(world, pos.x, pos.y, pos.z, item.getItem().copy());
            copy.setDefaultPickUpDelay();
            real = copy;
        } else if (spawned instanceof ExperienceOrb orb) {
            real = new ExperienceOrb(world, pos.x, pos.y, pos.z, orb.getValue());
        } else {
            LOGGER.debug("Contraption sim [{}] didn't spawn {} from the simulation", label, spawned);
            return false;
        }
        return world.addFreshEntity(real);
    }

    private void reportTrainPower() {
        if (!(entity instanceof CarriageContraptionEntity carriageEntity) || carriageEntity.getCarriage() == null) return;
        Carriage carriage = carriageEntity.getCarriage();
        Train train = carriage.train;

        drive = creativeEngine ? 1 : 0;
        for (TrainEngineBlockEntity engine : engines) drive = Math.max(drive, engine.driveFraction());
        TrainPower.report(train, carriage, drive, engines.size());

        if (ticks % LOAD_UPDATE_INTERVAL == 1 && !engines.isEmpty()) {
            float load = (float) (TorqueRailsConfig.STRESS_PER_CARRIAGE.get() * train.carriages.size() / Math.max(1, TrainPower.engineCount(train)));
            for (TrainEngineBlockEntity engine : engines) engine.setTrainLoad(load);
        }
    }

    void markChanged(BlockPos pos) {
        changed.add(pos);
    }

    boolean changeBlockState(BlockPos pos, BlockState newState) {
        return changeBlockState(pos, newState, 0);
    }

    private boolean changeBlockState(BlockPos pos, BlockState newState, int depth) {
        StructureBlockInfo info = contraption.getBlocks().get(pos);
        BlockState current = level.getBlockState(pos);
        if (info == null || current == newState) return false;
        if (current.getBlock() != newState.getBlock()) {
            deniedBlockChanges++;
            LOGGER.debug("Contraption sim [{}] refused block change at {}: {} -> {}", label, pos, current, newState);
            return false;
        }

        entity.setBlock(pos, new StructureBlockInfo(pos, newState, info.nbt()));
        ActorStates.update(contraption, pos, newState);
        BlockEntity be = level.blockEntities.get(pos);
        if (be != null) {
            be.setBlockState(newState);
            changed.add(pos);
        }
        blockStateChanges++;

        if (depth < MAX_SHAPE_UPDATE_DEPTH) updateNeighbourShapes(pos, newState, depth);
        return true;
    }

    private void updateNeighbourShapes(BlockPos pos, BlockState state, int depth) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbour = level.getBlockState(neighbourPos);
            if (neighbour.isAir()) continue;
            BlockState updated = neighbour.updateShape(direction.getOpposite(), state, level, neighbourPos, pos);
            if (updated != neighbour) changeBlockState(neighbourPos, updated, depth + 1);
        }
    }

    public Map<BlockPos, CompoundTag> collectClientChanges(boolean fullKinetics) {
        Map<BlockPos, CompoundTag> updates = new HashMap<>();
        Map<BlockPos, CompoundTag> updateTags = ((ContraptionAccessor) contraption).createtorquerails$getUpdateTags();

        for (BlockPos pos : changed) {
            BlockEntity be = level.blockEntities.get(pos);
            StructureBlockInfo info = contraption.getBlocks().get(pos);
            if (be == null || info == null) continue;
            CompoundTag nbt = be.saveWithFullMetadata();
            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");
            contraption.getBlocks().put(pos, new StructureBlockInfo(pos, info.state(), nbt));
            CompoundTag updateTag = be.getUpdateTag();
            updateTags.put(pos, updateTag);
            updates.put(pos, updateTag);
            writeBacks++;
        }
        changed.clear();

        for (KineticBlockEntity be : kineticBlockEntities) {
            KineticState state = new KineticState(be.getTheoreticalSpeed(), be.isOverStressed());
            KineticState previous = lastSyncedKinetics.put(be.getBlockPos(), state);
            if (fullKinetics || !state.equals(previous)) updates.computeIfAbsent(be.getBlockPos(), $ -> be.getUpdateTag());
        }
        return updates;
    }

    private void dumpState() {
        LOGGER.debug("Contraption sim [{}] state after {} ticks:", label, ticks);
        for (KineticBlockEntity be : kineticBlockEntities) LOGGER.debug("Contraption sim [{}]   {} {} speed={} network={} source={} overStressed={}", label, be.getBlockPos(), be.getBlockState().getBlock(), be.getTheoreticalSpeed(), be.network, be.source, be.isOverStressed());
        for (BlockEntity be : level.blockEntities.values())
            if (be instanceof FluidTankBlockEntity tank && tank.isController() && tank.boiler.isActive()) {
                BoilerData boiler = tank.boiler;
                LOGGER.debug("Contraption sim [{}]   boiler at {}: engines={} activeHeat={} passiveHeat={} " + "waterSupply={} engineEfficiency={}", label, tank.getBlockPos(), boiler.attachedEngines, boiler.activeHeat, boiler.passiveHeat, boiler.waterSupply, boiler.getEngineEfficiency(tank.getTotalTankSize()));
            }
        for (StorageBridge<?> bridge : storageBridges) LOGGER.debug("Contraption sim [{}]   storage at {}: {}", label, bridge.pos, bridge.describe(level));
        logSummary();
    }

    private void logSummary() {
        LOGGER.debug("Contraption sim [{}] after {} ticks: {} ticked, storage pulls={} pushes={}, block state " + "changes={} refused={}, block entity write-backs={}, engine drive={}", label, ticks, ticking.size(), storagePulls, storagePushes, blockStateChanges, deniedBlockChanges, writeBacks, drive);
        for (TrainEngineBlockEntity engine : engines) LOGGER.debug("Contraption sim [{}]   train engine at {}: speed={} overStressed={} load={} SU/RPM drive={}", label, engine.getBlockPos(), engine.getSpeed(), engine.isOverStressed(), engine.calculateStressApplied(), engine.driveFraction());
    }

    public void destroy() {
        LOGGER.debug("Contraption sim [{}] stopped", label);
        logSummary();
        Create.TORQUE_PROPAGATOR.onUnloadWorld(level);
        SimulationHooks.stop(level);
        level.blockEntities.clear();
        ticking.clear();
        kineticBlockEntities.clear();
        storageBridges.clear();
        lastSyncedKinetics.clear();
        changed.clear();
    }
}
