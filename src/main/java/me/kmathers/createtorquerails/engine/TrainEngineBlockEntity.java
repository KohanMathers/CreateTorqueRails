package me.kmathers.createtorquerails.engine;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import me.kmathers.createtorquerails.CreateTorqueRails;
import me.kmathers.createtorquerails.TorqueRailsConfig;
import me.kmathers.createtorquerails.client.EngineGoggleInfo;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrainEngineBlockEntity extends KineticBlockEntity {

    private float trainLoad = -1;

    public TrainEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float driveFraction() {
        if (isOverStressed()) return 0;
        return (float) Math.min(1, Math.abs(getSpeed()) / TorqueRailsConfig.FULL_SPEED_RPM.get());
    }

    public float theoreticalDriveFraction() {
        float load = load();
        if (load <= 0 || !IRotate.StressImpact.isEnabled()) return 1;
        float available = capacity - stress + load * Math.abs(getTheoreticalSpeed());
        return (float) Mth.clamp(available / load / TorqueRailsConfig.FULL_SPEED_RPM.get(), 0, 1);
    }

    public void setTrainLoad(float load) {
        if (load == trainLoad) return;
        trainLoad = load;
        if (hasNetwork()) {
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            getOrCreateNetwork().updateStress();
        }
        setChanged();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        Float topSpeed = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> EngineGoggleInfo::trainTopSpeed);
        if (topSpeed == null) return true;
        new LangBuilder(CreateTorqueRails.MODID).translate("gui.goggles.train_engine").forGoggles(tooltip);
        speedLine(tooltip, "gui.goggles.max_speed", driveFraction() * topSpeed);
        speedLine(tooltip, "gui.goggles.theoretical_max_speed", theoreticalDriveFraction() * topSpeed);
        return true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("TrainLoad", trainLoad);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        trainLoad = compound.contains("TrainLoad") ? compound.getFloat("TrainLoad") : -1;
    }

    private static void speedLine(List<Component> tooltip, String key, float blocksPerSecond) {
        new LangBuilder(CreateTorqueRails.MODID).translate(key).style(ChatFormatting.GRAY).space().add(new LangBuilder(CreateTorqueRails.MODID).translate("gui.goggles.speed", String.format("%.1f", blocksPerSecond)).style(ChatFormatting.AQUA)).forGoggles(tooltip, 1);
    }

    @Override
    public float calculateStressApplied() {
        float impact = load();
        lastStressApplied = impact;
        return impact;
    }

    private float load() {
        return trainLoad >= 0 ? trainLoad : TorqueRailsConfig.STRESS_PER_CARRIAGE.get().floatValue();
    }
}
