package me.melontini.andromeda.base.events;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.config.BootstrapConfig;
import me.melontini.andromeda.base.util.config.VerifiedConfig;

public interface ConfigEvent {

  static <T extends VerifiedConfig, M extends Module> Bus<ConfigEvent> bootstrap(M module) {
    return module.getOrCreateBus(
        "bootstrap_config_event",
        () -> new Bus<>(
            events -> (manager, config) -> events.forEach(event -> event.accept(manager, config))));
  }

  void accept(ModuleManager manager, BootstrapConfig config);
}
