package me.kmathers.createtorquerails.mixin;

import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import me.kmathers.createtorquerails.engine.TrainPower;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(Train.class)
public abstract class TrainMixin {

    private static final String DRIVE_KEY = "CreateTorqueRailsDrive";

    @Inject(method = "maxSpeed", at = @At("RETURN"), cancellable = true)
    private void createtorquerails$limitTopSpeed(CallbackInfoReturnable<Float> cir) {
        if (needsEngine()) cir.setReturnValue(cir.getReturnValueF() * TrainPower.driveFraction((Train) (Object) this));
    }

    @Inject(method = "maxTurnSpeed", at = @At("RETURN"), cancellable = true)
    private void createtorquerails$limitTurnSpeed(CallbackInfoReturnable<Float> cir) {
        if (needsEngine()) cir.setReturnValue(cir.getReturnValueF() * TrainPower.driveFraction((Train) (Object) this));
    }

    private boolean needsEngine() {
        return EffectiveSide.get().isServer() && !(this instanceof IHandcarTrain handcar && handcar.railways$isHandcar());
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void createtorquerails$saveDrive(DimensionPalette dimensions, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putFloat(DRIVE_KEY, TrainPower.driveFraction((Train) (Object) this));
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void createtorquerails$loadDrive(CompoundTag tag, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions, CallbackInfoReturnable<Train> cir) {
        if (tag.contains(DRIVE_KEY)) TrainPower.restore(cir.getReturnValue(), tag.getFloat(DRIVE_KEY));
    }
}
