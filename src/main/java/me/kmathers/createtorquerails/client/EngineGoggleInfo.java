package me.kmathers.createtorquerails.client;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.infrastructure.config.AllConfigs;

public final class EngineGoggleInfo {

    private EngineGoggleInfo() {}

    public static float trainTopSpeed() {
        ContraptionTargeting.Target target = ContraptionTargeting.goggleTarget;
        Carriage carriage = target == null ? null : target.entity().getCarriage();
        if (carriage != null && carriage.train != null) return carriage.train.maxSpeed() * 20;
        return AllConfigs.server().trains.trainTopSpeed.getF();
    }
}
