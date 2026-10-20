package me.kmathers.createtorquerails.client.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class TrainEngineScenes {

    private TrainEngineScenes() {}

    public static void trainEngine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("train_engine", "Powering trains with the Train Engine");
        scene.configureBasePlate(1, 0, 9);
        scene.scaleSceneView(.75f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();

        for (int x = 10; x >= 0; x--) {
            scene.world().showSection(util.select().position(x, 1, 4), Direction.DOWN);
            scene.idle(1);
        }
        scene.idle(10);

        BlockPos motorPos = util.grid().at(3, 4, 4);
        BlockPos enginePos = util.grid().at(5, 4, 4);
        BlockPos bogeyPos = util.grid().at(5, 2, 4);
        Selection carriage = util.select().fromTo(3, 2, 3, 7, 3, 5);
        Selection motor = util.select().fromTo(3, 4, 4, 4, 4, 4);
        Selection drive = util.select().fromTo(3, 4, 4, 5, 4, 4);

        ElementLink<WorldSectionElement> train = scene.world().showIndependentSection(carriage, Direction.DOWN);
        scene.idle(15);
        scene.world().showSectionAndMerge(util.select().position(enginePos), Direction.DOWN, train);
        scene.idle(15);

        scene.overlay().showText(70).text("Trains can't move without a working Train Engine on board").attachKeyFrame().pointAt(util.vector().topOf(enginePos)).placeNearTarget();
        scene.idle(80);

        scene.world().showSectionAndMerge(motor, Direction.DOWN, train);
        scene.idle(10);
        scene.world().setKineticSpeed(drive, 64);
        scene.effects().rotationSpeedIndicator(motorPos);
        scene.idle(10);

        scene.overlay().showText(70).text("It takes rotation through its front and back, like a shaft").attachKeyFrame().pointAt(util.vector().blockSurface(enginePos, Direction.WEST)).placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70).text("Its RPM sets how fast the train can go. At 64 RPM, it only reaches a quarter of its top speed").attachKeyFrame().pointAt(util.vector().topOf(enginePos)).placeNearTarget();
        scene.idle(20);
        scene.world().moveSection(train, util.vector().of(2, 0, 0), 60);
        scene.world().animateBogey(bogeyPos, -2, 60);
        scene.idle(70);

        scene.world().setKineticSpeed(drive, 256);
        scene.effects().rotationSpeedIndicator(motorPos.east(2));
        scene.idle(10);
        scene.overlay().showText(60).text("At 256 RPM, it reaches its full top speed").attachKeyFrame().pointAt(util.vector().topOf(enginePos.east(2))).placeNearTarget();
        scene.idle(20);
        scene.world().moveSection(train, util.vector().of(-4, 0, 0), 30);
        scene.world().animateBogey(bogeyPos, 4, 30);
        scene.idle(50);

        BlockPos engineNow = enginePos.west(2);
        scene.overlay().showText(90).text("Every carriage adds stress it has to carry: 16 SU per RPM, shared evenly between all of the train's engines").attachKeyFrame().pointAt(util.vector().topOf(engineNow)).placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(60).text("If an engine overstresses, it stops pulling").colored(PonderPalette.RED).pointAt(util.vector().topOf(engineNow)).placeNearTarget();
        scene.idle(70);

        scene.overlay().showControls(util.vector().topOf(engineNow), Pointing.DOWN, 80).withItem(AllItems.GOGGLES.asStack());
        scene.idle(10);
        scene.overlay().showText(80).text("With Engineer's Goggles, the engine shows the train's max speed, and how fast its stress capacity could take it").attachKeyFrame().pointAt(util.vector().topOf(engineNow)).placeNearTarget();
        scene.idle(90);

        scene.markAsFinished();
    }
}
