package me.melontini.andromeda.mixin.items.balanced_mending;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.items.balanced_mending.BalancedMending;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
@Feature("balancedMending")
class ExperienceOrbMixin {
    @Unique
    private static final BalancedMending am$balmend = ModuleManager.quick(BalancedMending.class);
    @Inject(at = @At("HEAD"), method = "repairPlayerGears", cancellable = true)
    private void andromeda$repair(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        if (am$balmend.config().enabled) cir.setReturnValue(amount);
    }
}
