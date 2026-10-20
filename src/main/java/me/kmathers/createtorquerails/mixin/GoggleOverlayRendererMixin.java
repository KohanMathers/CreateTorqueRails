package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import me.kmathers.createtorquerails.client.ContraptionTargeting;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GoggleOverlayRenderer.class)
public class GoggleOverlayRendererMixin {

    @ModifyVariable(method = "renderOverlay", at = @At("STORE"), ordinal = 0)
    private static BlockEntity createtorquerails$useCarriageBlockEntity(BlockEntity worldBlockEntity) {
        ContraptionTargeting.Target target = ContraptionTargeting.find();
        ContraptionTargeting.goggleTarget = target;
        return target != null ? target.blockEntity() : worldBlockEntity;
    }
}
