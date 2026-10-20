package me.kmathers.createtorquerails.simulation;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SimulationHooks {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Hook> HOOKS = new CopyOnWriteArrayList<>();

    public interface Hook {
        void onStart(Level level);

        void afterTick(Level level);

        void onStop(Level level);
    }

    private SimulationHooks() {}

    public static void register(Hook hook) {
        HOOKS.add(hook);
    }

    static void start(Level level) {
        for (Hook hook : HOOKS) run(hook, "start", () -> hook.onStart(level));
    }

    static void afterTick(Level level) {
        for (Hook hook : HOOKS) run(hook, "tick", () -> hook.afterTick(level));
    }

    static void stop(Level level) {
        for (Hook hook : HOOKS) run(hook, "stop", () -> hook.onStop(level));
    }

    private static void run(Hook hook, String stage, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOGGER.error("Contraption sim hook {} failed on {}", hook.getClass().getSimpleName(), stage, e);
        }
    }
}
