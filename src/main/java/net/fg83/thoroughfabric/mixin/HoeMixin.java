package net.fg83.thoroughfabric.mixin;

import net.fg83.thoroughfabric.StepCountData;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
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

        context.getWorld().getBlockState(pos);
        if (!context.getWorld().isClient){
            StepCountData stepCountData = StepCountData.get(
                    Objects.requireNonNull(
                            Objects.requireNonNull(context.getWorld().getServer()).getWorld(context.getWorld().getRegistryKey())
                    )
            );
            stepCountData.resetStepCount(pos);
        }
    }

}
