package me.melontini.andromeda.modules.entities.vehicle_unentrapment;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;

@ModuleInfo(name = "vehicle_unentrapment", category = "entities", environment = Environment.SERVER)
public final class VehicleUnentrapment extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG =
      new ConfigDefinition<>(() -> GameConfig.class);

  VehicleUnentrapment() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> Main::init);
  }
}
