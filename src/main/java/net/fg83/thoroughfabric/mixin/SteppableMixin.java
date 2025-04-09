package net.fg83.thoroughfabric.mixin;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.AreaLib;
import net.fg83.thoroughfabric.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.fg83.thoroughfabric.TFUtils.*;

@Mixin(Block.class)
public class SteppableMixin {
    @Inject(method = "onSteppedOn", at = @At("RETURN"))
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (world.isClient) return;

        MinecraftServer server = world.getServer();
        if (server == null) return;

        Block stateBlock = state.getBlock();
        if (!affectedBlocks.contains(stateBlock)) {
            return;
        }


        StepCountData stepCountData = StepCountData.get((ServerWorld) world);
        ServerPlayerEntity player = getPlayerFromEntity(entity);
        if (player == null || player.interactionManager.getGameMode() == GameMode.ADVENTURE) return;

        UUID playerId = player.getUuid();

        BlockPos currentLocation = player.getBlockPos();
        BlockPos storedLocation = PlayerLocationTracker.getPlayerLocation(playerId);

        if (storedLocation == null) {
            PlayerLocationTracker.updatePlayerLocation(playerId, currentLocation);
            return;
        }

        if (storedLocation.equals(currentLocation)){
            return;
        }
        else {
            PlayerLocationTracker.updatePlayerLocation(playerId, currentLocation);
        }

        if (ConfigManager.getConfig().useAreaRestrictions){
            AtomicBoolean isPathable = new AtomicBoolean(false);

            ConfigManager.getConfig().areaIds.forEach(areaId -> {
                if (!isPathable.get()){
                    Area testArea = AreaLib.getServerArea(server, Identifier.of(areaId));
                    if (testArea != null && testArea.contains(world, Vec3d.of(pos))){
                        isPathable.compareAndSet(false, true);
                    }
                }
            });
            if (!isPathable.get()){
                return;
            }
            else {
                isPathable.compareAndSet(true, false);
            }
        }

        int stepWeight = calculateStepWeight(entity);
        stepCountData.incrementStepCount(pos, stepWeight);

        if (checkBlockUpdate(world, pos, state)) {
            doBlockUpdate(world, pos);
        }
    }

    @Inject(method = "onPlaced", at = @At("HEAD"))
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci){
        if (!world.isClient && affectedBlocks.contains(state.getBlock())){
            StepCountData stepCountData = StepCountData.get((ServerWorld) world);
            stepCountData.resetStepCount(pos);
        }
    }
}
