package me.melontini.andromeda.modules.items.tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.VerifiedConfig;

@ModuleInfo(name = "tooltips", category = "items", environment = Environment.CLIENT)
public final class Tooltips extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Tooltips() {
    this.defineConfig(ConfigState.CLIENT, CONFIG);
  }

  public static final class Config extends VerifiedConfig {
    public boolean clock = true;
    public boolean compass = true;
    public boolean recoveryCompass = true;
  }
}
