package me.melontini.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.melontini.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import me.melontini.andromeda.modules.entities.ghast_tweaks.GhastTweaks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireballEntity.class)
abstract class FireballEntityMixin extends AbstractFireballEntity {

  public FireballEntityMixin(EntityType<? extends AbstractFireballEntity> entityType, World world) {
    super(entityType, world);
  }

  @WrapOperation(
      method = "onCollision",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"))
  private Explosion redirectExplosionType(
      World instance,
      Entity entity,
      double x,
      double y,
      double z,
      float power,
      boolean createFire,
      World.ExplosionSourceType explosionSourceType,
      Operation<Explosion> original) {
    if (this.getOwner() instanceof GhastEntity
        && instance.am$get(GhastTweaks.CONFIG).fireBallsConvertObsidian) {
      Explosion.DestructionType destructionType =
          instance.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)
              ? instance.getGameRules().getBoolean(GameRules.MOB_EXPLOSION_DROP_DECAY)
                  ? Explosion.DestructionType.DESTROY_WITH_DECAY
                  : Explosion.DestructionType.DESTROY
              : Explosion.DestructionType.KEEP;

      Explosion explosion =
          new Explosion(instance, entity, null, null, x, y, z, power, createFire, destructionType);
      ((GhastExplosionDuck) explosion).andromeda$convertObsidian(true);
      explosion.collectBlocksAndDamageEntities();
      explosion.affectWorld(true);
      return explosion;
    } else {
      return original.call(instance, entity, x, y, z, power, createFire, explosionSourceType);
    }
  }
}
