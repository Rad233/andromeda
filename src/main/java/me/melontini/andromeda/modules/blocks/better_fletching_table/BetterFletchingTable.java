package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.VerifiedConfig;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "better_fletching_table", category = "blocks")
public final class BetterFletchingTable extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  BetterFletchingTable() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> FletchingScreenHandler.init(this));
    InitEvent.client(this).listen(() -> FletchingScreen::onClient);
  }

  public static final class Config extends VerifiedConfig {
    public DoubleIntermediary divergenceModifier = DoubleIntermediary.of(0.2);
  }
}
