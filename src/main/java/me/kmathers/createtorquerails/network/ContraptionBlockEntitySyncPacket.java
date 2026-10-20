package me.kmathers.createtorquerails.network;

import me.kmathers.createtorquerails.client.ClientContraptionSync;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record ContraptionBlockEntitySyncPacket(int entityId, Map<BlockPos, CompoundTag> updateTags) {

    public static void encode(ContraptionBlockEntitySyncPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeMap(packet.updateTags, FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::writeNbt);
    }

    public static ContraptionBlockEntitySyncPacket decode(FriendlyByteBuf buf) {
        return new ContraptionBlockEntitySyncPacket(buf.readVarInt(), buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readNbt));
    }

    public static void handle(ContraptionBlockEntitySyncPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientContraptionSync.apply(packet));
    }
}
