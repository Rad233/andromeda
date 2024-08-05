package me.melontini.andromeda.modules.entities.ghast_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.melontini.andromeda.modules.entities.ghast_tweaks.GhastExplosionDuck;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
abstract class ExplosionMixin implements GhastExplosionDuck {

  @Shadow
  @Final
  private World world;

  @Unique private final ObjectArrayList<BlockPos> affectedObsidian = new ObjectArrayList<>();

  @Unique private boolean affectObsidian = false;

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/explosion/ExplosionBehavior;getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;"),
      method = "collectBlocksAndDamageEntities")
  private void collectAffectedObsidian(
      CallbackInfo ci,
      @Local(index = 14) float h,
      @Local(index = 22) BlockPos pos,
      @Local(index = 23) BlockState state) {
    if (!affectObsidian || state.getBlock() != Blocks.OBSIDIAN) return;
    if (h - 0.64 > 0 && world.random.nextFloat() >= 0.2f) affectedObsidian.add(pos);
  }

  @Inject(
      at = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion;createFire:Z"),
      method = "affectWorld")
  private void affectObsidian(boolean particles, CallbackInfo ci) {
    for (BlockPos blockPos : affectedObsidian) {
      world.setBlockState(blockPos, Blocks.CRYING_OBSIDIAN.getDefaultState());
    }
  }

  @Override
  public void andromeda$convertObsidian(boolean b) {
    this.affectObsidian = b;
  }
}
