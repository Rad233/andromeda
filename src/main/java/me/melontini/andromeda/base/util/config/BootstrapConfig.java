package me.melontini.andromeda.base.util.config;

import org.jetbrains.annotations.NotNull;

public final class BootstrapConfig extends VerifiedConfig {
  public boolean enabled = false;

  public static @NotNull BootstrapConfig create(boolean on) {
    var config = new BootstrapConfig();
    if (on) config.enabled = true;
    return config;
  }

  @Override
  public BootstrapConfig copy() {
    return (BootstrapConfig) super.copy();
  }
}
