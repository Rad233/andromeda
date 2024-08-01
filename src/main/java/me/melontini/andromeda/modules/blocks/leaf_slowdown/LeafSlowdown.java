package me.melontini.andromeda.modules.blocks.leaf_slowdown;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;

@Deprecated
@ModuleInfo(name = "leaf_slowdown", category = "blocks", environment = Environment.SERVER)
public final class LeafSlowdown extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG =
      new ConfigDefinition<>(() -> GameConfig.class);

  LeafSlowdown() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }
}
