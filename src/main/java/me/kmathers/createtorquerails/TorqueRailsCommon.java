package me.kmathers.createtorquerails;

public final class TorqueRailsCommon {

    private static boolean bypassEngine = false;

    private TorqueRailsCommon() {}

    public static void toggleBypassEngine() {
        bypassEngine = !bypassEngine;
    }

    public static boolean getBypassEngine() {
        return bypassEngine;
    }
}
