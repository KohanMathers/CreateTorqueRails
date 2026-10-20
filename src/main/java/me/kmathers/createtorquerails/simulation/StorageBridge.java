package me.kmathers.createtorquerails.simulation;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract sealed class StorageBridge<S> {

    final BlockPos pos;
    final BlockState state;
    final BlockEntity blockEntity;
    @Nullable
    private List<S> snapshot;

    StorageBridge(BlockPos pos, BlockState state, BlockEntity blockEntity) {
        this.pos = pos;
        this.state = state;
        this.blockEntity = blockEntity;
    }

    boolean pull(Level level) {
        List<S> stored = storageContents();
        if (snapshot != null && sameContents(stored, snapshot)) return false;
        unmountInto(level);
        snapshot = stored;
        return true;
    }

    static <T> T detachedCopy(Codec<T> codec, T storage) {
        Tag tag = codec.encodeStart(NbtOps.INSTANCE, storage).getOrThrow(false, error -> {});
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, error -> {});
    }

    abstract String describeContents(List<S> contents);

    String describe(Level level) {
        List<S> simulated = blockEntityContents(level);
        return "storage " + describeContents(storageContents()) + ", block entity " + (simulated == null ? "<doesn't mount>" : describeContents(simulated));
    }

    boolean push(Level level) {
        List<S> simulated = blockEntityContents(level);
        if (simulated == null || snapshot != null && sameContents(simulated, snapshot)) return false;
        writeToStorage(simulated);
        snapshot = storageContents();
        return true;
    }

    abstract List<S> storageContents();

    @Nullable
    abstract List<S> blockEntityContents(Level level);

    abstract boolean sameContents(List<S> a, List<S> b);

    abstract void unmountInto(Level level);

    abstract void writeToStorage(List<S> contents);

    static final class Fluids extends StorageBridge<FluidStack> {
        private final MountedFluidStorage storage;

        private Fluids(BlockPos pos, BlockState state, BlockEntity be, MountedFluidStorage storage) {
            super(pos, state, be);
            this.storage = storage;
        }

        @Nullable
        static Fluids create(Level level, BlockPos pos, BlockState state, BlockEntity be, MountedFluidStorage storage) {
            Fluids bridge = new Fluids(pos, state, be, storage);
            return bridge.blockEntityContents(level) != null ? bridge : null;
        }

        private static List<FluidStack> contents(IFluidHandler handler) {
            List<FluidStack> contents = new ArrayList<>(handler.getTanks());
            for (int i = 0; i < handler.getTanks(); i++) contents.add(handler.getFluidInTank(i).copy());
            return contents;
        }

        @Override
        List<FluidStack> storageContents() {
            return contents(storage);
        }

        @Nullable
        @Override
        List<FluidStack> blockEntityContents(Level level) {
            MountedFluidStorage mounted = storage.type.mount(level, state, pos, blockEntity);
            return mounted != null && mounted.getTanks() == storage.getTanks() ? contents(mounted) : null;
        }

        @Override
        boolean sameContents(List<FluidStack> a, List<FluidStack> b) {
            if (a.size() != b.size()) return false;
            for (int i = 0; i < a.size(); i++) if (!a.get(i).isFluidStackIdentical(b.get(i))) return false;
            return true;
        }

        @Override
        void unmountInto(Level level) {
            detachedCopy(MountedFluidStorage.CODEC, storage).unmount(level, state, pos, blockEntity);
        }

        @Override
        String describeContents(List<FluidStack> contents) {
            StringBuilder description = new StringBuilder();
            for (FluidStack fluid : contents) description.append(fluid.isEmpty() ? "[empty]" : "[" + ForgeRegistries.FLUIDS.getKey(fluid.getFluid()) + " " + fluid.getAmount() + "mB]");
            return description.toString();
        }

        @Override
        void writeToStorage(List<FluidStack> contents) {
            for (int i = 0; i < storage.getTanks(); i++) storage.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            for (FluidStack fluid : contents) if (!fluid.isEmpty()) storage.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    static final class Items extends StorageBridge<ItemStack> {
        private final MountedItemStorage storage;

        private Items(BlockPos pos, BlockState state, BlockEntity be, MountedItemStorage storage) {
            super(pos, state, be);
            this.storage = storage;
        }

        @Nullable
        static Items create(Level level, BlockPos pos, BlockState state, BlockEntity be, MountedItemStorage storage) {
            Items bridge = new Items(pos, state, be, storage);
            return bridge.blockEntityContents(level) != null ? bridge : null;
        }

        private static List<ItemStack> contents(IItemHandler handler) {
            List<ItemStack> contents = new ArrayList<>(handler.getSlots());
            for (int i = 0; i < handler.getSlots(); i++) contents.add(handler.getStackInSlot(i).copy());
            return contents;
        }

        @Override
        List<ItemStack> storageContents() {
            return contents(storage);
        }

        @Nullable
        @Override
        List<ItemStack> blockEntityContents(Level level) {
            MountedItemStorage mounted = storage.type.mount(level, state, pos, blockEntity);
            return mounted != null && mounted.getSlots() == storage.getSlots() ? contents(mounted) : null;
        }

        @Override
        boolean sameContents(List<ItemStack> a, List<ItemStack> b) {
            if (a.size() != b.size()) return false;
            for (int i = 0; i < a.size(); i++) if (!ItemStack.matches(a.get(i), b.get(i))) return false;
            return true;
        }

        @Override
        void unmountInto(Level level) {
            detachedCopy(MountedItemStorage.CODEC, storage).unmount(level, state, pos, blockEntity);
        }

        @Override
        String describeContents(List<ItemStack> contents) {
            int filled = 0, items = 0;
            for (ItemStack stack : contents) if (!stack.isEmpty()) {
                filled++;
                items += stack.getCount();
            }
            return items + " items in " + filled + "/" + contents.size() + " slots";
        }

        @Override
        void writeToStorage(List<ItemStack> contents) {
            for (int i = 0; i < contents.size(); i++) storage.setStackInSlot(i, contents.get(i).copy());
        }
    }
}
