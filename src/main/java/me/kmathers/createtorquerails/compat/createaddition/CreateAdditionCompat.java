package me.kmathers.createtorquerails.compat.createaddition;

import com.mrh0.createaddition.energy.network.EnergyNetworkManager;
import me.kmathers.createtorquerails.simulation.SimulationHooks;
import net.minecraft.world.level.Level;

public final class CreateAdditionCompat implements SimulationHooks.Hook {

    public static void register() {
        SimulationHooks.register(new CreateAdditionCompat());
    }

    @Override
    public void onStart(Level level) {
        new EnergyNetworkManager(level);
    }

    @Override
    public void afterTick(Level level) {
        EnergyNetworkManager.tickWorld(level);
    }

    @Override
    public void onStop(Level level) {
        EnergyNetworkManager.instances.remove(level);
    }
}
