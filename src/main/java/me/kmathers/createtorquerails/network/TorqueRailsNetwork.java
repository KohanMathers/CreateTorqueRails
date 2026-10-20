package me.kmathers.createtorquerails.network;

import me.kmathers.createtorquerails.CreateTorqueRails;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class TorqueRailsNetwork {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(CreateTorqueRails.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private TorqueRailsNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(ContraptionBlockEntitySyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(ContraptionBlockEntitySyncPacket::encode).decoder(ContraptionBlockEntitySyncPacket::decode).consumerMainThread(ContraptionBlockEntitySyncPacket::handle).add();
    }
}
