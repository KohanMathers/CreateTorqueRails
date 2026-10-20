package me.kmathers.createtorquerails.engine;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class TrainPower {

    private static final class State {
        final Map<Carriage, Float> drive = new WeakHashMap<>();
        final Map<Carriage, Integer> engines = new WeakHashMap<>();
        @Nullable
        Float saved;
    }

    private static final Map<Train, State> STATES = new WeakHashMap<>();

    private TrainPower() {}

    public static void report(Train train, Carriage carriage, float driveFraction, int engineCount) {
        State state = STATES.computeIfAbsent(train, $ -> new State());
        state.drive.put(carriage, driveFraction);
        state.engines.put(carriage, engineCount);
        if (state.drive.size() >= train.carriages.size()) state.saved = null;
    }

    public static float driveFraction(Train train) {
        State state = STATES.get(train);
        if (state == null) return 0;
        float best = state.saved != null ? state.saved : 0;
        for (float drive : state.drive.values()) best = Math.max(best, drive);
        return best;
    }

    public static int engineCount(Train train) {
        State state = STATES.get(train);
        if (state == null) return 0;
        int count = 0;
        for (int engines : state.engines.values()) count += engines;
        return count;
    }

    public static void restore(Train train, float driveFraction) {
        STATES.computeIfAbsent(train, $ -> new State()).saved = driveFraction;
    }
}
