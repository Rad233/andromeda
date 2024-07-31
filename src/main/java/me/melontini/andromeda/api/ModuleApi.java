package me.melontini.andromeda.api;

import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provided by modules.
 */
@ApiStatus.NonExtendable
public interface ModuleApi<I, O> extends Function<I, O> {

  @Override
  O apply(I input);
}
