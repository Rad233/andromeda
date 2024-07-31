package me.melontini.andromeda.api;

import java.util.function.BiPredicate;
import lombok.experimental.UtilityClass;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

@UtilityClass
public class Routes {

  @UtilityClass
  public static class AdvancementGeneration {
    public static final ApiRoute<BiPredicate<Identifier, Recipe<?>>, Void> RECIPE_FILTER =
        new ApiRoute<>("recipe_filter", ApiRoute.Status.STABLE);
  }

  @UtilityClass
  public static class GuardedLoot {
    public static final ApiRoute<BiPredicate<BlockEntity, PlayerEntity>, Void> UNLOCKER =
        new ApiRoute<>("unlocker", ApiRoute.Status.EXPERIMENTAL);
  }
}
