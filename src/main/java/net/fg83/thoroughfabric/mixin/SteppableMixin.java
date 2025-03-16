package net.fg83.thoroughfabric.mixin;

import net.fg83.thoroughfabric.Footstep;

import net.fg83.thoroughfabric.StepCountData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Block.class)
public class SteppableMixin {
    @Inject(method = "onSteppedOn", at = @At("HEAD"))
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (!world.isClient){
            if (entity instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                return;
            }

            if (entity.hasPassengers() && entity.getFirstPassenger() instanceof ServerPlayerEntity rider && rider.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                return;
            }

            new Footstep(world, pos, state, entity).process();
        }
    }
    @Inject(method = "onPlaced", at = @At("HEAD"))
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci){
        if (!world.isClient && Footstep.tfTestBlock(world, pos)){
            StepCountData stepCountData = StepCountData.get(
                    Objects.requireNonNull(
                            Objects.requireNonNull(world.getServer()).getWorld(world.getRegistryKey())
                    )
            );
            stepCountData.resetStepCount(pos);
        }
    }

}
