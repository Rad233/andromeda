package me.melontini.andromeda.modules.gui.smooth_tooltips;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.VerifiedConfig;

@ModuleInfo(name = "smooth_tooltips", category = "gui", environment = Environment.CLIENT)
public final class SmoothTooltips extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  SmoothTooltips() {
    this.defineConfig(ConfigState.CLIENT, CONFIG);
  }

  public static final class Config extends VerifiedConfig {
    public int clampX = 30;
    public int clampY = 30;
    public double deltaX = 0.3;
    public double deltaY = 0.3;
  }
}
