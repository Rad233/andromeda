package me.melontini.andromeda.api;

import java.util.function.Consumer;
import me.melontini.andromeda.base.ModuleManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides a "safe" way to interact with modules.
 * The idea is the ability to support different modules without directly interacting with volatile classes or semantics.
 * <br/>
 * Available routes can be found in {@link Routes}.
 */
@ApiStatus.Experimental
public interface ModuleApiProvider {

  /**
   * The provider is only available after {@code preLaunch}.
   * @return The {@link ModuleApiProvider}.
   */
  static ModuleApiProvider getInstance() {
    return ModuleManager.get();
  }

  <I, O> void whenAvailable(String module, ApiRoute<I, O> route, ApiConsumer<I, O> consumer);

  @FunctionalInterface
  interface ApiConsumer<I, O> extends Consumer<ModuleApi<I, O>> {
    @Override
    void accept(ModuleApi<I, O> api);
  }
}
