package me.kmathers.createtorquerails;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TorqueRailsConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue STRESS_PER_CARRIAGE;
    public static final ForgeConfigSpec.DoubleValue FULL_SPEED_RPM;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        STRESS_PER_CARRIAGE = builder.comment("Stress impact (SU per RPM) each carriage puts on the train's engines, shared evenly between", "them. At 256 RPM the default 16 costs 4096 SU per carriage.").defineInRange("stressPerCarriage", 16.0, 0.0, 1024.0);
        FULL_SPEED_RPM = builder.comment("Engine RPM at which a train reaches its full top speed; slower engines scale it down linearly.").defineInRange("fullSpeedRPM", 256.0, 1.0, 256.0);
        SPEC = builder.build();
    }

    private TorqueRailsConfig() {}
}
