package me.kmathers.createtorquerails.assembly;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public final class TrainFluidCapture {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ThreadLocal<Contraption> ASSEMBLING = new ThreadLocal<>();

    private TrainFluidCapture() {
    }

    public static void register() {
        BlockMovementChecks.registerMovementNecessaryCheck(TrainFluidCapture::isMovementNecessary);
    }

    public static void assemblyStarted(Contraption contraption) {
        ASSEMBLING.set(contraption);
    }

    public static void assemblyFinished() {
        ASSEMBLING.remove();
    }

    private static CheckResult isMovementNecessary(BlockState state, Level level, BlockPos pos) {
        if (!(ASSEMBLING.get() instanceof CarriageContraption) || !(state.getBlock() instanceof LiquidBlock)) return CheckResult.PASS;
        boolean source = state.getFluidState().isSource();
        LOGGER.debug("Train fluid capture: assembly reached {} at {} (source={}, movementAllowed={})", state, pos, source, BlockMovementChecks.isMovementAllowed(state, level, pos));
        return source ? CheckResult.SUCCESS : CheckResult.PASS;
    }
}
