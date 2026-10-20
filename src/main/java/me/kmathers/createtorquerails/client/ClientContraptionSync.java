package me.kmathers.createtorquerails.client;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import me.kmathers.createtorquerails.network.ContraptionBlockEntitySyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.slf4j.Logger;

import java.util.Map;

public final class ClientContraptionSync {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientContraptionSync() {}

    public static void apply(ContraptionBlockEntitySyncPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity entity)) {
            LOGGER.debug("Contraption sim client: no contraption entity #{} for sync of {} blocks", packet.entityId(), packet.updateTags().size());
            return;
        }
        Contraption contraption = entity.getContraption();
        if (contraption == null) {
            LOGGER.debug("Contraption sim client: entity #{} has no contraption yet", packet.entityId());
            return;
        }

        int applied = 0;
        boolean speedChanged = false;
        for (Map.Entry<BlockPos, CompoundTag> entry : packet.updateTags().entrySet()) {
            BlockPos pos = entry.getKey();
            StructureBlockInfo info = contraption.getBlocks().get(pos);
            if (info != null) contraption.getBlocks().put(pos, new StructureBlockInfo(pos, info.state(), entry.getValue().copy()));

            BlockEntity be = contraption.getBlockEntityClientSide(pos);
            if (be == null) continue;
            float before = be instanceof KineticBlockEntity kbe ? kbe.getSpeed() : 0;
            be.handleUpdateTag(entry.getValue());
            applied++;
            if (be instanceof KineticBlockEntity kbe) speedChanged |= kbe.getSpeed() != before;
        }

        LOGGER.debug("Contraption sim client: entity #{} received {} blocks, applied to {}, speedChanged={}", packet.entityId(), packet.updateTags().size(), applied, speedChanged);

        if (speedChanged) contraption.invalidateClientContraptionChildren();
    }
}
