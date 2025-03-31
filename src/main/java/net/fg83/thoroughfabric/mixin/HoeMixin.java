package net.fg83.thoroughfabric.mixin;

import net.fg83.thoroughfabric.StepCountData;
import net.fg83.thoroughfabric.TFUtils;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(HoeItem.class)
public class HoeMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir){
        BlockPos pos = new BlockPos(
                (int) Math.round(context.getHitPos().x),
                (int) Math.round(context.getHitPos().y),
                (int) Math.round(context.getHitPos().z)
        );

        if (!context.getWorld().isClient){
            if (TFUtils.affectedBlocks.contains(context.getWorld().getBlockState(pos).getBlock())){
                StepCountData stepCountData = StepCountData.get((ServerWorld) context.getWorld());
                if (stepCountData.getStepCount(pos) > 0){
                    stepCountData.resetStepCount(pos);
                    Objects.requireNonNull(context.getPlayer()).playSound(SoundEvent.of(Identifier.of("item.brush.brushing.gravel.complete")));
                    cir.cancel();
                }
            }
        }
    }

}
