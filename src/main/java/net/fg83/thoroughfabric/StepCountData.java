package net.fg83.thoroughfabric;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Manages step count data associated with block positions in a Minecraft world.
 */
public class StepCountData extends PersistentState {

    // A map to store step counts associated with block positions
    private Map<BlockPos, Integer> stepCounts = new HashMap<>();

    /**
     * Default constructor for StepCountData.
     */
    public StepCountData() {}

    /**
     * Increments the step count for the given block position by a specified multiplier.
     *
     * @param pos   The block position.
     * @param multi The multiplier to increment by.
     */
    public void incrementStepCount(BlockPos pos, int multi) {
        stepCounts.put(pos, stepCounts.getOrDefault(pos, 0) + multi);
        markDirty(); // Marks the state as dirty to indicate it has changed
    }

    /**
     * Resets the step count for the given block position to zero.
     *
     * @param pos The block position.
     */
    public void resetStepCount(BlockPos pos) {
        stepCounts.put(pos, 0);
        markDirty(); // Marks the state as dirty to indicate it has changed
    }

    /**
     * Gets the step count for the given block position.
     *
     * @param pos The block position.
     * @return The step count.
     */
    public int getStepCount(BlockPos pos) {
        return stepCounts.getOrDefault(pos, 0);
    }

    /**
     * Writes the step count data to an Nbt compound for saving.
     *
     * @param nbt            The Nbt compound to write to.
     * @param registryLookup Registry wrapper lookup.
     * @return The modified Nbt compound.
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList stepCountList = new NbtList();
        for (Map.Entry<BlockPos, Integer> entry : stepCounts.entrySet()) {
            NbtCompound compound = new NbtCompound();
            compound.putLong("pos", entry.getKey().asLong());
            compound.putInt("count", entry.getValue());
            stepCountList.add(compound);
        }
        nbt.put("stepCounts", stepCountList);
        return nbt;
    }

    /**
     * Reads the step count data from an Nbt compound.
     *
     * @param nbt The Nbt compound to read from.
     */
    public void readFromNbt(NbtCompound nbt) {
        NbtList stepCountList = nbt.getList("stepCounts", 10);
        for (int i = 0; i < stepCountList.size(); i++) {
            NbtCompound compound = stepCountList.getCompound(i);
            BlockPos pos = BlockPos.fromLong(compound.getLong("pos"));
            int count = compound.getInt("count");
            stepCounts.put(pos, count);
        }
    }

    /**
     * Creates a new PersistentState.Type for StepCountData.
     *
     * @return The PersistentState.Type for StepCountData.
     */
    private static PersistentState.Type<StepCountData> createType() {
        Supplier<StepCountData> constructor = StepCountData::new;
        BiFunction<NbtCompound, RegistryWrapper.WrapperLookup, StepCountData> deserializer = (nbt, wrapper) -> {
            StepCountData data = new StepCountData();
            data.readFromNbt(nbt);
            return data;
        };
        return new PersistentState.Type<>(constructor, deserializer, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    /**
     * Retrieves the StepCountData instance from the persistent state manager of the given world.
     *
     * @param world The server world.
     * @return The StepCountData instance.
     */
    public static StepCountData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(createType(), "stepCounts");
    }
}


