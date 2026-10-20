package me.kmathers.createtorquerails.client.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import me.kmathers.createtorquerails.CreateTorqueRails;
import me.kmathers.createtorquerails.TorqueRailsBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class TorqueRailsPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateTorqueRails.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(TorqueRailsBlocks.TRAIN_ENGINE.getId()).addStoryBoard("train_engine", TrainEngineScenes::trainEngine);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.addToTag(AllCreatePonderTags.TRAIN_RELATED).add(TorqueRailsBlocks.TRAIN_ENGINE.getId());
    }
}
