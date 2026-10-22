package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import me.kmathers.createtorquerails.simulation.ActorStates;
import me.kmathers.createtorquerails.simulation.ContraptionSimulationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContraptionEntity.class)
public abstract class AbstractContraptionEntityMixin {

    @Inject(method = "handlePlayerInteraction", at = @At("RETURN"), cancellable = true)
    private void createtorquerails$useSimulatedBlock(Player player, BlockPos localPos, Direction side, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        AbstractContraptionEntity self = (AbstractContraptionEntity) (Object) this;
        if (!(self instanceof CarriageContraptionEntity) || self.getContraption() == null) return;
        if (self.getContraption().getSeats().contains(localPos) || AllItems.WRENCH.isIn(player.getItemInHand(hand))) return;

        if (self.level().isClientSide) cir.setReturnValue(true);
        else cir.setReturnValue(ContraptionSimulationManager.useBlock(self, player, localPos, side, hand));
    }

    @Inject(method = "writeAdditional", at = @At("HEAD"))
    private void createtorquerails$saveSimulation(CompoundTag compound, boolean spawnPacket, CallbackInfo ci) {
        AbstractContraptionEntity self = (AbstractContraptionEntity) (Object) this;
        if (!spawnPacket && !self.level().isClientSide) ContraptionSimulationManager.save(self);
    }

    @Inject(method = "handleBlockChange", at = @At("TAIL"))
    private void createtorquerails$updateActorState(BlockPos localPos, BlockState newState, CallbackInfo ci) {
        AbstractContraptionEntity self = (AbstractContraptionEntity) (Object) this;
        if (self.getContraption() != null) ActorStates.update(self.getContraption(), localPos, newState);
    }
}
