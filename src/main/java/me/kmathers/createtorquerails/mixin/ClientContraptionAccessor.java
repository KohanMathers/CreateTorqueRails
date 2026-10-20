package me.kmathers.createtorquerails.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientContraption.class)
public interface ClientContraptionAccessor {

    @Accessor("contraption")
    Contraption createtorquerails$getContraption();
}
