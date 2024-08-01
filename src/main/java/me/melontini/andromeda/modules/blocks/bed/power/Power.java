package me.melontini.andromeda.modules.blocks.bed.power;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "bed/power", category = "blocks", environment = Environment.SERVER)
public final class Power extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Power() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }

  public static class Config extends GameConfig {
    public DoubleIntermediary power = DoubleIntermediary.of(5);
  }
}
