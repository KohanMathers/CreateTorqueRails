package me.kmathers.createtorquerails.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CreativeTrainEngineBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CreativeTrainEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TrainEngineBlock.BASE, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TrainEngineBlock.BASE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean sneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return TrainEngineBlock.withBase(defaultBlockState().setValue(FACING, sneaking ? context.getNearestLookingDirection() : context.getNearestLookingDirection().getOpposite()), context);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return TrainEngineBlock.rotateBase(state.setValue(FACING, rotation.rotate(state.getValue(FACING))), rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return TrainEngineBlock.mirrorFacingAndBase(state, mirror);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TrainEngineBlock.shape(state);
    }
}
