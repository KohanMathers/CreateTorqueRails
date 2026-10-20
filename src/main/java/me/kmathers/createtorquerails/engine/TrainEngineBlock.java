package me.kmathers.createtorquerails.engine;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import me.kmathers.createtorquerails.TorqueRailsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrainEngineBlock extends DirectionalKineticBlock implements IBE<TrainEngineBlockEntity> {

    public static final DirectionProperty BASE = DirectionProperty.create("base", Direction.Plane.HORIZONTAL);

    private static final VoxelShape SHAPE_X = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 3, 3, 16, 13, 13));
    private static final VoxelShape SHAPE_Z = Shapes.or(Block.box(0, 0, 0, 16, 3, 16), Block.box(3, 3, 0, 13, 13, 16));
    private static final VoxelShape FRAME_Y = Block.box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape SHAPE_Y_NORTH = Shapes.or(Block.box(0, 0, 0, 16, 16, 3), FRAME_Y);
    private static final VoxelShape SHAPE_Y_SOUTH = Shapes.or(Block.box(0, 0, 13, 16, 16, 16), FRAME_Y);
    private static final VoxelShape SHAPE_Y_WEST = Shapes.or(Block.box(0, 0, 0, 3, 16, 16), FRAME_Y);
    private static final VoxelShape SHAPE_Y_EAST = Shapes.or(Block.box(13, 0, 0, 16, 16, 16), FRAME_Y);

    public TrainEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BASE, Direction.NORTH));
    }

    public static VoxelShape shape(BlockState state) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> SHAPE_X;
            case Z -> SHAPE_Z;
            case Y -> switch (state.getValue(BASE)) {
                case SOUTH -> SHAPE_Y_SOUTH;
                case WEST -> SHAPE_Y_WEST;
                case EAST -> SHAPE_Y_EAST;
                default -> SHAPE_Y_NORTH;
            };
        };
    }

    public static BlockState withBase(BlockState state, BlockPlaceContext context) {
        if (state == null || state.getValue(FACING).getAxis() != Direction.Axis.Y) return state;
        return state.setValue(BASE, context.getHorizontalDirection().getOpposite());
    }

    public static BlockState rotateBase(BlockState state, Rotation rotation) {
        return state.setValue(BASE, rotation.rotate(state.getValue(BASE)));
    }

    public static BlockState mirrorFacingAndBase(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING))).setValue(BASE, mirror.mirror(state.getValue(BASE)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BASE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withBase(super.getStateForPlacement(context), context);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return rotateBase(super.rotate(state, rotation), rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return mirrorFacingAndBase(state, mirror);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<TrainEngineBlockEntity> getBlockEntityClass() {
        return TrainEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrainEngineBlockEntity> getBlockEntityType() {
        return TorqueRailsBlocks.TRAIN_ENGINE_BLOCK_ENTITY.get();
    }
}
