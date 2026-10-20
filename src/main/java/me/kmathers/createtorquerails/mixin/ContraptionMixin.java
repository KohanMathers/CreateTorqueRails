package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import me.kmathers.createtorquerails.assembly.TrainFluidCapture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Contraption.class)
public abstract class ContraptionMixin {

    @Inject(method = "searchMovedStructure", at = @At("HEAD"))
    private void createtorquerails$assemblyStarted(Level level, BlockPos pos, Direction forcedDirection, CallbackInfoReturnable<Boolean> cir) {
        TrainFluidCapture.assemblyStarted((Contraption) (Object) this);
    }

    @Inject(method = "searchMovedStructure", at = @At("RETURN"))
    private void createtorquerails$assemblyFinished(Level level, BlockPos pos, Direction forcedDirection, CallbackInfoReturnable<Boolean> cir) {
        TrainFluidCapture.assemblyFinished();
    }

    @Redirect(method = "removeBlocksFromWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;f_49990_:Lnet/minecraft/world/level/block/Block;", opcode = Opcodes.GETSTATIC, remap = false))
    private Block createtorquerails$takeWaterlogging() {
        return (Object) this instanceof CarriageContraption ? Blocks.AIR : Blocks.WATER;
    }

    @Redirect(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;m_61124_(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;", remap = false))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object createtorquerails$keepCarriedWaterlogging(BlockState state, Property property, Comparable value) {
        if ((Object) this instanceof CarriageContraption && property == BlockStateProperties.WATERLOGGED && state.getValue(BlockStateProperties.WATERLOGGED)) value = Boolean.TRUE;
        return state.setValue(property, value);
    }
}
