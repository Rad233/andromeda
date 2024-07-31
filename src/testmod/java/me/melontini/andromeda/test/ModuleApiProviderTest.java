package me.melontini.andromeda.test;

import com.mojang.logging.LogUtils;
import me.melontini.andromeda.api.ModuleApiProvider;
import me.melontini.andromeda.api.Routes;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class ModuleApiProviderTest implements ModInitializer {

  private static final Logger LOGGER = LogUtils.getLogger();

  @Override
  public void onInitialize() {
    ModuleApiProvider provider = ModuleApiProvider.getInstance();

    provider.whenAvailable(
        "misc/recipe_advancements_generation",
        Routes.AdvancementGeneration.RECIPE_FILTER,
        api -> api.apply((identifier, recipe) -> {
          if (identifier.getNamespace().contains("b")) {
            LOGGER.info(identifier.toString());
            return true;
          }
          return false;
        }));
  }
}
