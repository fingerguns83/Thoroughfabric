package net.fg83.thoroughfabric;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TFUtils {
    private static final int DEFAULT_MULTIPLIER = 1;
    private static final Map<EntityType, Function<TFConfig, Integer>> MULTIPLIER_MAP = initializeMultiplierMap();

    public static List<EntityType> rideableEntities = List.of(
            EntityType.HORSE,
            EntityType.SKELETON_HORSE,
            EntityType.CAMEL,
            EntityType.MULE,
            EntityType.PIG,
            EntityType.DONKEY,
            EntityType.ZOMBIE_HORSE,
            EntityType.LLAMA,
            EntityType.TRADER_LLAMA,
            EntityType.STRIDER
    );

    public static List<Block> affectedBlocks = List.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.MYCELIUM,
            Blocks.PODZOL,
            Blocks.COARSE_DIRT,
            Blocks.DIRT_PATH
    );

    private static final Map<Block, Function<TFConfig, Integer>> BLOCK_CONFIG_MAP = Map.of(
            Blocks.GRASS_BLOCK, config -> config.grassReps,
            Blocks.DIRT, config -> config.grassReps,
            Blocks.MYCELIUM, config -> config.grassReps,
            Blocks.PODZOL, config -> config.grassReps,
            Blocks.COARSE_DIRT, config -> config.coarseDirtReps
    );

    private static Map<EntityType, Function<TFConfig, Integer>> initializeMultiplierMap() {
        return Map.of(
                EntityType.HORSE, config -> config.horseMulti,
                EntityType.ZOMBIE_HORSE, config -> config.horseMulti,
                EntityType.SKELETON_HORSE, config -> config.horseMulti,
                EntityType.LLAMA, config -> config.llamaMulti,
                EntityType.TRADER_LLAMA, config -> config.llamaMulti,
                EntityType.MULE, config -> config.muleMulti,
                EntityType.CAMEL, config -> config.camelMulti,
                EntityType.DONKEY, config -> config.donkeyMulti,
                EntityType.PIG, config -> config.pigMulti,
                EntityType.STRIDER, config -> config.striderMulti
        );
    }

    public static ServerPlayerEntity getPlayerFromEntity(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            return (ServerPlayerEntity) entity;
        }
        if (rideableEntities.contains(entity.getType()) && entity.hasPassengers()) {
            Entity firstPassenger = entity.getFirstPassenger();
            if (firstPassenger instanceof ServerPlayerEntity) {
                return (ServerPlayerEntity) firstPassenger;
            }
        }
        return null;
    }

    public static int calculateStepWeight(Entity entity) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return getMultiplier(entity);
        }
        return 1;
    }


    public static int getMultiplier(Entity entity) {
        TFConfig config = ConfigManager.getConfig();
        return MULTIPLIER_MAP
                .getOrDefault(entity.getType(), c -> DEFAULT_MULTIPLIER)
                .apply(config);
    }

    public static int getMaxSteps(Block block) {
        TFConfig config = ConfigManager.getConfig();

        if (BLOCK_CONFIG_MAP.containsKey(block)) {
            return BLOCK_CONFIG_MAP.get(block).apply(config);
        }

        if (testPathWear(block, config)) {
            return config.pathReps;
        }

        return -1; // Default case
    }

    private static boolean testPathWear(Block block, TFConfig config) {
        return block == Blocks.DIRT_PATH && config.pathsWear;
    }

    private static boolean isGrassRelated(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT) ||
                state.isOf(Blocks.MYCELIUM) || state.isOf(Blocks.PODZOL);
    }

    private static BlockState determineNewState(BlockState currentState) {
        if (isGrassRelated(currentState)) {
            return Blocks.COARSE_DIRT.getDefaultState();
        } else if (currentState.isOf(Blocks.COARSE_DIRT)) {
            return Blocks.DIRT_PATH.getDefaultState();
        } else if (currentState.isOf(Blocks.DIRT_PATH)) {
            return Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
        }
        return currentState;
    }

    public static boolean checkBlockUpdate(World world, BlockPos pos, BlockState state){
        StepCountData stepCountData = StepCountData.get((ServerWorld) world);

        int maxSteps = getMaxSteps(state.getBlock());
        return maxSteps != -1 && stepCountData.getStepCount(pos) >= maxSteps;
    }

    public static void doBlockUpdate(World world, BlockPos pos){
        BlockState currentState = world.getBlockState(pos);
        BlockState newState = determineNewState(currentState);

        // Only change the block state if it differs from the current state, with a random chance
        if (newState != currentState) {
            int chance = (int) (Math.random() * 5); // 1 in 5 chance
            if (chance == 0) {
                world.setBlockState(pos, newState);
                StepCountData stepCountData = StepCountData.get((ServerWorld) world);
                stepCountData.resetStepCount(pos);
            }
        }
    }

}
