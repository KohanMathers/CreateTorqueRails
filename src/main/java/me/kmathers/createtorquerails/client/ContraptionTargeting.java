package me.kmathers.createtorquerails.client;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;

public final class ContraptionTargeting {

    public record Target(CarriageContraptionEntity entity, BlockHitResult hit, BlockEntity blockEntity) {}

    @Nullable
    public static Target goggleTarget;

    private ContraptionTargeting() {}

    @Nullable
    public static Target find() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        Map<Integer, WeakReference<AbstractContraptionEntity>> contraptions = ContraptionHandler.loadedContraptions.get(mc.level);
        if (contraptions == null || contraptions.isEmpty()) return null;

        Couple<Vec3> rayInputs = ContraptionHandlerClient.getRayInputs(mc.player);
        Vec3 origin = rayInputs.getFirst();
        Vec3 target = rayInputs.getSecond();
        AABB rayBounds = new AABB(origin, target).inflate(16);

        double bestDistance = Double.MAX_VALUE;
        Target best = null;
        for (WeakReference<AbstractContraptionEntity> ref : contraptions.values()) {
            if (!(ref.get() instanceof CarriageContraptionEntity entity) || entity.getContraption() == null) continue;
            if (!entity.getBoundingBox().intersects(rayBounds)) continue;
            BlockHitResult hit = ContraptionHandlerClient.rayTraceContraption(origin, target, entity);
            if (hit == null) continue;
            double distance = entity.toGlobalVector(hit.getLocation(), 1).distanceTo(origin);
            if (distance >= bestDistance) continue;
            BlockEntity be = entity.getContraption().getBlockEntityClientSide(hit.getBlockPos());
            if (be == null) continue;
            bestDistance = distance;
            best = new Target(entity, hit, be);
        }
        return best;
    }
}
