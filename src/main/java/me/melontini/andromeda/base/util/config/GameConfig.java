package me.melontini.andromeda.base.util.config;

import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class GameConfig extends VerifiedConfig {
  @ConfigEntry.Gui.Excluded
  public BooleanIntermediary available = BooleanIntermediary.of(true);

  @Override
  public GameConfig copy() {
    return (GameConfig) super.copy();
  }
}
