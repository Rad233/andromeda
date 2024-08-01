package me.melontini.andromeda.base.util.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import lombok.SneakyThrows;
import me.melontini.dark_matter.api.mixin.AsmUtil;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.apache.commons.lang3.ClassUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Verifies a config class upon creation.<br/>
 * This class also provides a dynamic implementation for {@link #equals(Object)}, {@link #toString()}, {@link #hashCode()} and {@link #copy()}.<br/>
 * This not really necessary, as compile-time extensions would be faster, but it's way more fun to do it this way :P
 */
public abstract class VerifiedConfig {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final Map<Class<?>, ClassBootstrap> CACHE =
      Collections.synchronizedMap(new IdentityHashMap<>());

  @ConfigEntry.Gui.Excluded
  private transient final ClassBootstrap classData;

  public VerifiedConfig() {
    this.classData = CACHE.computeIfAbsent(this.getClass(), cls -> {
      for (Field field : this.getClass().getFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        if ("classData".equals(field.getName())) continue;
        if (Modifier.isFinal(field.getModifiers())) // We use no-arg constructors, so final fields would be impossible to use in copy()
          throw new IllegalStateException("All config fields must not be final!");
      }
      return bootstrap((Class<? extends VerifiedConfig>) cls);
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

  private interface ClassBootstrap {
    boolean impl_areFieldsEqual(VerifiedConfig o1, VerifiedConfig o2);

    int impl_hashFields(VerifiedConfig config);

    void impl_appendFields(VerifiedConfig config, StringJoiner joiner);

    void impl_copyFields(VerifiedConfig from, VerifiedConfig to);
  }

  private static final Map<Class<?>, BiConsumer<InstructionAdapter, Label>> COMPARE_TYPES =
      ImmutableMap.of(
          int.class,
          InstructionAdapter::ificmpeq,
          boolean.class,
          InstructionAdapter::ificmpeq,
          long.class,
          (adapter, label) -> {
            adapter.lcmp();
            adapter.ifeq(label);
          },
          float.class,
          (adapter, label) -> {
            adapter.visitInsn(Opcodes.FCMPL);
            adapter.ifeq(label);
          },
          double.class,
          (adapter, label) -> {
            adapter.visitInsn(Opcodes.DCMPL);
            adapter.ifeq(label);
          });

  @SneakyThrows
  private static ClassBootstrap bootstrap(Class<? extends VerifiedConfig> cls) {
    ClassNode node = new ClassNode();
    String internalName =
        VerifiedConfig.class.getPackageName().replace('.', '/') + "/ClassBootstrap$"
            + Type.getInternalName(cls)
                .substring("me.melontini.andromeda.".length())
                .replace('/', '_');
    node.visit(
        Opcodes.V17,
        Opcodes.ACC_PRIVATE,
        internalName,
        null,
        Type.getInternalName(Object.class),
        new String[] {Type.getInternalName(ClassBootstrap.class)});

    // public no args constructor.
    AsmUtil.insAdapter(node, Opcodes.ACC_PUBLIC, "<init>", "()V", adapter -> {
      adapter.load(0, Type.getType(Object.class));
      adapter.invokespecial(Type.getInternalName(Object.class), "<init>", "()V", false);
      adapter.areturn(Type.VOID_TYPE);
    });

    List<Field> fields = Arrays.stream(cls.getFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .filter(field -> !"classData".equals(field.getName()))
        .toList();

    Method areFieldsEqual = ClassBootstrap.class.getMethod(
        "impl_areFieldsEqual", VerifiedConfig.class, VerifiedConfig.class);
    AsmUtil.insAdapter(
        node,
        Opcodes.ACC_PUBLIC,
        areFieldsEqual.getName(),
        Type.getMethodDescriptor(areFieldsEqual),
        adapter -> {
          Label label = new Label();
          for (Field field : fields) {
            adapter.visitLabel(label);

            adapter.load(1, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.getfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));
            adapter.load(2, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.getfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));

            label = new Label();
            if (field.getType().isPrimitive()) {
              COMPARE_TYPES.get(field.getType()).accept(adapter, label);
            } else {
              adapter.invokestatic(
                  Type.getInternalName(Objects.class),
                  "equals",
                  "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                  false);
              adapter.ifne(label);
            }
            adapter.iconst(0);
            adapter.areturn(Type.BOOLEAN_TYPE);
          }
          adapter.visitLabel(label);
          adapter.iconst(1);
          adapter.areturn(Type.BOOLEAN_TYPE);
        });

    Method hashFields = ClassBootstrap.class.getMethod("impl_hashFields", VerifiedConfig.class);
    AsmUtil.insAdapter(
        node,
        Opcodes.ACC_PUBLIC,
        hashFields.getName(),
        Type.getMethodDescriptor(hashFields),
        adapter -> {
          adapter.iconst(fields.size());
          adapter.newarray(Type.getType(Object.class));

          for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            adapter.dup();
            adapter.iconst(i);
            adapter.load(1, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.getfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));

            if (field.getType().isPrimitive()) {
              var wrapper = ClassUtils.primitiveToWrapper(field.getType());
              adapter.invokestatic(
                  Type.getInternalName(wrapper),
                  "valueOf",
                  "(%s)%s"
                      .formatted(Type.getDescriptor(field.getType()), Type.getDescriptor(wrapper)),
                  false);
            }
            adapter.astore(Type.getType(Object.class));
          }
          adapter.invokestatic(
              Type.getInternalName(Objects.class), "hash", "([Ljava/lang/Object;)I", false);
          adapter.areturn(Type.INT_TYPE);
        });

    Method appendFields = ClassBootstrap.class.getMethod(
        "impl_appendFields", VerifiedConfig.class, StringJoiner.class);
    AsmUtil.insAdapter(
        node,
        Opcodes.ACC_PUBLIC,
        appendFields.getName(),
        Type.getMethodDescriptor(appendFields),
        adapter -> {
          Label label = new Label();
          for (Field field : fields) {
            adapter.visitLabel(label);

            adapter.load(2, Type.getType(StringJoiner.class));
            adapter.aconst(field.getName() + "=");
            adapter.load(1, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.getfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));

            label = new Label();
            if (field.getType().isPrimitive()) {
              adapter.invokestatic(
                  Type.getInternalName(String.class),
                  "valueOf",
                  "(%s)Ljava/lang/String;".formatted(Type.getDescriptor(field.getType())),
                  false);
            } else {
              adapter.invokestatic(
                  Type.getInternalName(String.class),
                  "valueOf",
                  "(%s)Ljava/lang/String;".formatted(Type.getDescriptor(Object.class)),
                  false);
            }
            adapter.invokevirtual(
                Type.getInternalName(String.class),
                "concat",
                "(Ljava/lang/String;)Ljava/lang/String;",
                false);
            adapter.invokevirtual(
                Type.getInternalName(StringJoiner.class),
                "add",
                "(Ljava/lang/CharSequence;)Ljava/util/StringJoiner;",
                false);
            adapter.pop();
          }
          adapter.visitLabel(label);
          adapter.areturn(Type.VOID_TYPE);
        });

    Method copyFields = ClassBootstrap.class.getMethod(
        "impl_copyFields", VerifiedConfig.class, VerifiedConfig.class);
    AsmUtil.insAdapter(
        node,
        Opcodes.ACC_PUBLIC,
        copyFields.getName(),
        Type.getMethodDescriptor(copyFields),
        adapter -> {
          for (Field field : fields) {
            adapter.load(2, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.load(1, Type.getType(VerifiedConfig.class));
            adapter.checkcast(Type.getType(cls));
            adapter.getfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));
            adapter.putfield(
                Type.getInternalName(cls), field.getName(), Type.getDescriptor(field.getType()));
          }
          adapter.areturn(Type.VOID_TYPE);
        });

    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    var lookup = LOOKUP.defineHiddenClass(writer.toByteArray(), true);
    return (ClassBootstrap) lookup
        .findConstructor(lookup.lookupClass(), MethodType.methodType(void.class))
        .invoke();
  }
}
