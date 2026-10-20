package me.kmathers.createtorquerails.simulation;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.apache.commons.lang3.tuple.MutablePair;

public final class ActorStates {

    private ActorStates() {}

    public static void update(Contraption contraption, BlockPos localPos, BlockState state) {
        for (MutablePair<StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            StructureBlockInfo info = actor.getLeft();
            if (!info.pos().equals(localPos)) continue;
            actor.setLeft(new StructureBlockInfo(info.pos(), state, info.nbt()));
            if (actor.getRight() != null) actor.getRight().state = state;
        }
    }
}
