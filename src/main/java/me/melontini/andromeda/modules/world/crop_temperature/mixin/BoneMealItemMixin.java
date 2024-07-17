package me.melontini.andromeda.modules.world.crop_temperature.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperature;
import me.melontini.andromeda.modules.world.crop_temperature.PlantTemperatureData;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
abstract class BoneMealItemMixin {

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    private void andromeda$useOnFertilizable(ItemUsageContext ctx, CallbackInfoReturnable<ActionResult> cir) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        if (world.isClient()) return;

        BlockState state = world.getBlockState(pos);
        if (world.am$get(PlantTemperature.CONFIG).affectBoneMeal.asBoolean(LootContextUtil.block(world, Vec3d.ofCenter(pos), state))) {
            if (!PlantTemperatureData.roll(pos, state, world.getBiome(pos).value().getTemperature(), (ServerWorld) world)) {
                ModuleManager.quick(PlantTemperature.class).logger().info(state.getBlock());
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
