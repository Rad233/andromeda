package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;

@ModuleInfo(name = "zombie/clean_pickup", category = "entities", environment = Environment.SERVER)
public final class Pickup extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG =
      new ConfigDefinition<>(() -> GameConfig.class);

  Pickup() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }
}
