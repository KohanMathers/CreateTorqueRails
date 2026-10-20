package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.content.contraptions.render.ClientContraption;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.ForgeBlockModelBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContraptionVisual.class)
public abstract class ContraptionVisualMixin {

    @Redirect(method = "setupStructure", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/lib/model/baked/ForgeBlockModelBuilder;build()Ldev/engine_room/flywheel/lib/model/SimpleModel;"))
    private SimpleModel createtorquerails$renderCarriageFluids(ForgeBlockModelBuilder builder, ClientContraption clientContraption) {
        if (((ClientContraptionAccessor) clientContraption).createtorquerails$getContraption() instanceof CarriageContraption) builder.renderFluids(true);
        return builder.build();
    }
}
