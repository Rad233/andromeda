package me.melontini.andromeda.base.util.config;

import java.util.function.Supplier;

public record ConfigDefinition<T extends VerifiedConfig>(Supplier<Class<T>> supplier) {

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
