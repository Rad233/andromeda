package me.melontini.andromeda.modules.entities.better_furnace_minecart;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.VerifiedConfig;

@ModuleInfo(
    name = "better_furnace_minecart",
    category = "entities",
    environment = Environment.SERVER)
public final class BetterFurnaceMinecart extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BetterFurnaceMinecart() {
    this.defineConfig(ConfigState.MAIN, CONFIG);
  }

  public static final class Config extends VerifiedConfig {
    public int maxFuel = 45000;
    public boolean takeFuelWhenLow = true;
  }
}
