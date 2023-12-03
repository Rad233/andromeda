package me.melontini.andromeda.modules.entities.snowball_tweaks.mixin.freeze;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.entities.snowball_tweaks.Snowballs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
class SnowballEntityMixin {
    @Unique
    private static final Snowballs am$snow = ModuleManager.quick(Snowballs.class);

    @Inject(at = @At("TAIL"), method = "onEntityHit")
    private void andromeda$applyFreezing(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!am$snow.config().freeze) return;

        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setFrozenTicks(livingEntity.getMinFreezeDamageTicks() + 40);
        }
    }
}