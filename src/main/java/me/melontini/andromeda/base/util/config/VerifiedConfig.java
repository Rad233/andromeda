package me.melontini.andromeda.base.util.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import lombok.SneakyThrows;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Verifies a config class upon creation.<br/>
 * This class also provides a dynamic implementation for {@link #equals(Object)}, {@link #toString()}, {@link #hashCode()} and {@link #copy()}.<br/>
 * This not really necessary, as compile-time extensions would be faster, but it's way more fun to do it this way :P
 */
public abstract class VerifiedConfig {

  private static final Map<Class<?>, ClassBootstrap> CACHE =
      Collections.synchronizedMap(new IdentityHashMap<>());

  @ConfigEntry.Gui.Excluded
  private final transient ClassBootstrap classData;

  public VerifiedConfig() {
    this.classData = CACHE.computeIfAbsent(this.getClass(), cls -> {
      for (Field field : this.getClass().getFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        if (Modifier.isTransient(field.getModifiers())) continue;
        if (Modifier.isFinal(field.getModifiers()))
          // We use no-arg constructors, so final fields would be impossible to use in copy()
          throw new IllegalStateException("All config fields must not be final!");
      }
      return ClassBootstrapFactory.bootstrap((Class<? extends VerifiedConfig>) cls);
    });
  }

  /**
   * Creates a shallow copy of this config object.
   * @return The shallow copy.
   */
  @SneakyThrows
  public VerifiedConfig copy() {
    VerifiedConfig instance =
        (VerifiedConfig) this.getClass().getConstructors()[0].newInstance();
    this.classData.impl_copyFields(this, instance);
    return instance;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof VerifiedConfig vc)) return false;
    if (this.getClass() != vc.getClass()) return false;

    return this.classData.impl_areFieldsEqual(this, vc);
  }

  @Override
  public final int hashCode() {
    return this.classData.impl_hashFields(this);
  }

  @SneakyThrows
  @Override
  public final String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(
        this.getClass().getName().substring(this.getClass().getPackageName().length() + 1));

    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    this.classData.impl_appendFields(this, joiner);
    return builder.append(joiner).toString();
  }

  interface ClassBootstrap {
    boolean impl_areFieldsEqual(VerifiedConfig o1, VerifiedConfig o2);

    int impl_hashFields(VerifiedConfig config);

    void impl_appendFields(VerifiedConfig config, StringJoiner joiner);

    void impl_copyFields(VerifiedConfig from, VerifiedConfig to);
  }
}
