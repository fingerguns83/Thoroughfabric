package net.fg83.thoroughfabric;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class Footstep {
    World world;
    BlockPos pos;
    BlockState state;
    Entity entity;

    /**
     * Constructor for Footstep
     *
     * @param world  the world where the step is taking place
     * @param pos    the position of the block
     * @param state  the state of the block
     * @param entity the entity that caused the step
     */
    public Footstep(World world, BlockPos pos, BlockState state, Entity entity) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.entity = entity;
    }

    /**
     * Processes the footstep event. Updates player location or increments step count based on entity type and conditions.
     */
    public void process() {
        // If block is not among the predefined set, only update player location and return
        if (!tfTestBlock(world, pos)) {
            PlayerLocationTracker.updatePlayerLocation(entity.getUuid(), entity.getBlockPos());
            return;
        }
        int multi = 1; // Default multiplier for steps

        // Check if the entity is a player
        if (entity.getType().equals(EntityType.PLAYER)) {
            if (!PlayerLocationTracker.isMatch(entity)) { // If player's location has changed
                tfStepIncrement(world, pos, 1); // Increment step count
            }
            PlayerLocationTracker.updatePlayerLocation(entity.getUuid(), entity.getBlockPos());
            return;
        }

        // If the entity does not have a player passenger, return
        if (!entity.hasPassengers() || !Objects.requireNonNull(entity.getFirstPassenger()).getType().equals(EntityType.PLAYER)) {
            return;
        }

        TFConfig config = ConfigManager.getConfig();

        // Determine the multiplier based on the entity type
        if (entity.getType().equals(EntityType.HORSE) ||
                entity.getType().equals(EntityType.ZOMBIE_HORSE) ||
                entity.getType().equals(EntityType.SKELETON_HORSE)) {
            multi = config.horseMulti;
        } else if (entity.getType().equals(EntityType.LLAMA) ||
                entity.getType().equals(EntityType.TRADER_LLAMA)) {
            multi = config.llamaMulti;
        } else if (entity.getType().equals(EntityType.MULE)) {
            multi = config.muleMulti;
        } else if (entity.getType().equals(EntityType.CAMEL)) {
            multi = config.camelMulti;
        } else if (entity.getType().equals(EntityType.DONKEY)) {
            multi = config.donkeyMulti;
        } else if (entity.getType().equals(EntityType.PIG)) {
            multi = config.pigMulti;
        } else if (entity.getType().equals(EntityType.STRIDER)) {
            multi = config.striderMulti;
        } else {
            return;
        }

        // If entity's location does not match tracked location, increment step count
        if (!PlayerLocationTracker.isMatch(entity)) {
            tfStepIncrement(world, pos, multi);
        }
        PlayerLocationTracker.updatePlayerLocation(entity.getUuid(), entity.getBlockPos());
    }

    /**
     * Determines if the block at the given position is one of the predefined blocks.
     *
     * @param world the world where the block is located
     * @param pos   the position of the block
     * @return true if the block is among the predefined set, false otherwise
     */
    public static boolean tfTestBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.MYCELIUM ||
                block == Blocks.PODZOL || block == Blocks.COARSE_DIRT || block == Blocks.DIRT_PATH;
    }

    /**
     * Increments the step count for a block position and potentially changes the block's state after a threshold is reached.
     *
     * @param world the world where the block is located
     * @param pos   the position of the block
     * @param multi the multiplier for the step count based on the entity type
     */
    private void tfStepIncrement(World world, BlockPos pos, int multi) {
        MinecraftServer server = world.getServer();
        if (world.getServer() == null) {
            return;
        }

        assert server != null;

        StepCountData stepCountData = StepCountData.get(Objects.requireNonNull(server.getWorld(world.getRegistryKey())));
        stepCountData.incrementStepCount(pos, multi);

        int maxSteps;
        TFConfig config = ConfigManager.getConfig();

        Block block = world.getBlockState(pos).getBlock();

        // Determine the maximum number of steps based on the block type
        if (block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT ||
                block == Blocks.MYCELIUM ||
                block == Blocks.PODZOL) {
            maxSteps = config.grassReps;
        } else if (block == Blocks.COARSE_DIRT) {
            maxSteps = config.coarseDirtReps;
        } else if (block == Blocks.DIRT_PATH && config.pathsWear) {
            maxSteps = config.pathReps;
        } else {
            return;
        }

        // If the step count exceeds the maximum steps, change the block
        if (stepCountData.getStepCount(pos) >= maxSteps) {
            tfBlockChange(world, pos, stepCountData);
        }
    }

    /**
     * Changes the block at the given position to a new state based on its current state and resets the step count.
     *
     * @param world         the world where the block is located
     * @param pos           the position of the block
     * @param stepCountData the data structure holding the step counts for block positions
     */
    private void tfBlockChange(World world, BlockPos pos, StepCountData stepCountData) {
        BlockState currentState = world.getBlockState(pos);
        BlockState newState = currentState;

        // Determine the new block state based on the current block state
        if (currentState.isOf(Blocks.GRASS_BLOCK) || currentState.isOf(Blocks.DIRT) ||
                currentState.isOf(Blocks.MYCELIUM) || currentState.isOf(Blocks.PODZOL)) {
            newState = Blocks.COARSE_DIRT.getDefaultState();
        } else if (currentState.isOf(Blocks.COARSE_DIRT)) {
            newState = Blocks.DIRT_PATH.getDefaultState();
        } else if (currentState.isOf(Blocks.DIRT_PATH)) {
            newState = Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
        }

        // Only change the block state if it differs from the current state, with a random chance
        if (newState != currentState) {
            int chance = (int) (Math.random() * 5); // 1 in 5 chance
            if (chance == 0) {
                world.setBlockState(pos, newState);
                stepCountData.resetStepCount(pos);
            }
        }
    }
}
