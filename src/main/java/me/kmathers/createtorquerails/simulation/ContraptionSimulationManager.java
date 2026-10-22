package me.kmathers.createtorquerails.simulation;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import me.kmathers.createtorquerails.network.ContraptionBlockEntitySyncPacket;
import me.kmathers.createtorquerails.network.TorqueRailsNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ContraptionSimulationManager {

    private static final int SYNC_INTERVAL = 4;
    private static final int FULL_KINETIC_SYNC_INTERVAL = 40;

    private static final Map<AbstractContraptionEntity, ContraptionSimulation> SIMULATIONS = new IdentityHashMap<>();

    private ContraptionSimulationManager() {}

    public static void tick(AbstractContraptionEntity entity) {
        Contraption contraption = entity.getContraption();
        if (contraption == null) return;

        ContraptionSimulation simulation = SIMULATIONS.get(entity);
        if (simulation == null || simulation.contraption() != contraption) {
            if (simulation != null) simulation.destroy();
            simulation = new ContraptionSimulation(entity.getType().toShortString() + "#" + entity.getId(), entity, contraption);
            SIMULATIONS.put(entity, simulation);
        }

        simulation.tick();

        if (entity.tickCount % SYNC_INTERVAL != 0) return;
        Map<BlockPos, CompoundTag> changes = simulation.collectClientChanges(entity.tickCount % FULL_KINETIC_SYNC_INTERVAL == 0);
        if (!changes.isEmpty()) TorqueRailsNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ContraptionBlockEntitySyncPacket(entity.getId(), changes));
    }

    public static boolean useBlock(AbstractContraptionEntity entity, Player player, BlockPos localPos, Direction side, InteractionHand hand) {
        ContraptionSimulation simulation = SIMULATIONS.get(entity);
        return simulation != null && simulation.useBlock(player, localPos, side, hand);
    }

    public static void save(AbstractContraptionEntity entity) {
        ContraptionSimulation simulation = SIMULATIONS.get(entity);
        if (simulation != null && simulation.contraption() == entity.getContraption()) simulation.saveAll();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        SIMULATIONS.entrySet().removeIf(entry -> {
            if (!entry.getKey().isRemoved()) return false;
            entry.getValue().destroy();
            return true;
        });
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SIMULATIONS.values().forEach(ContraptionSimulation::destroy);
        SIMULATIONS.clear();
    }
}
