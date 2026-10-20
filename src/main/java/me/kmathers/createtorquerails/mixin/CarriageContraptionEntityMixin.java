package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import me.kmathers.createtorquerails.simulation.ContraptionSimulationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarriageContraptionEntity.class)
public abstract class CarriageContraptionEntityMixin {

    @Inject(method = "tickContraption", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/entity/CarriageContraptionEntity;tickActors()V", shift = At.Shift.AFTER))
    private void createtorquerails$tickSimulation(CallbackInfo ci) {
        AbstractContraptionEntity self = (AbstractContraptionEntity) (Object) this;
        if (!self.level().isClientSide) ContraptionSimulationManager.tick(self);
    }
}
