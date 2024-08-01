package me.melontini.andromeda.modules.entities.bee_flower_duplication;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;

@ModuleInfo(
    name = "bee_flower_duplication",
    category = "entities",
    environment = Environment.SERVER)
public final class BeeFlowerDuplication extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BeeFlowerDuplication() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }

  public static final class Config extends GameConfig {
    public BooleanIntermediary tallFlowers = BooleanIntermediary.of(true);
  }
}
