package me.melontini.andromeda.base;

import com.google.common.base.Suppliers;
import com.google.common.reflect.ClassPath;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.client.AndromedaClient;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import me.melontini.dark_matter.api.config.ConfigManager;
import net.fabricmc.api.EnvType;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ModuleManager {

    private static final Supplier<ModuleManager> INSTANCE = Suppliers.memoize(ModuleManager::new);
    private static final int modulePrefixLength = "me.melontini.andromeda.modules.".length();

    private final Map<Class<?>, ModuleInfo> modules = new LinkedHashMap<>();
    private final Map<String, ModuleInfo> moduleNames = new HashMap<>();
    private final Map<Class<?>, ConfigManager<? extends BasicConfig>> configs = new HashMap<>();

    public void collect() {
        List<Module<?>> list = new ArrayList<>();
        Utilities.supplyUnchecked(() -> ClassPath.from(ModuleManager.class.getClassLoader())).getTopLevelClassesRecursive("me.melontini.andromeda.modules")
                .stream().map(ClassPath.ClassInfo::getName).map(s -> Utilities.supplyUnchecked(() -> ClassInfo.forName(s)))
                .filter(ci -> ci.getInterfaces().contains(Module.class.getName().replace(".", "/")) ||
                        ci.getInterfaces().contains(BasicModule.class.getName().replace(".", "/")))
                .map(ci -> Utilities.supplyUnchecked(() -> Class.forName(ci.getClassName())))
                .map(cls -> Utilities.supplyUnchecked(() -> Reflect.setAccessible(cls.getDeclaredConstructor())))
                .forEach(ctx -> list.add((Module<?>) Utilities.supplyUnchecked(ctx::newInstance)));

        list.removeIf(m -> (m.environment() == Environment.CLIENT && CommonValues.environment() == EnvType.SERVER));
        list.forEach(m -> modules.put(m.getClass(), new ModuleInfo(m.getClass().getPackageName().substring(modulePrefixLength), m)));
        setUpConfigs();
        modules.values().removeIf(m -> !m.module().enabled());
        modules.values().forEach(m -> moduleNames.put(m.name(), m));
    }

    public void setUpConfigs() {
        modules.values().forEach(m -> {
            var config = ConfigBuilder.create(m.module().configClass(), CommonValues.mod(),
                            "andromeda/" + m.name().replace('.', '/'))
                    .traverseSuper(true);
            m.module().onConfig(Utilities.cast(config));
            configs.put(m.module().getClass(), config.build());
        });
    }

    public List<String> getMixins() {
        ClassPath p = Utilities.supplyUnchecked(() -> ClassPath.from(ModuleManager.class.getClassLoader()));

        List<String> mixins = new ArrayList<>();
        modules.values().forEach(m -> p.getTopLevelClassesRecursive("me.melontini.andromeda.mixin." + m.name()).stream()
                .map(ClassPath.ClassInfo::getName).forEach(mixins::add));
        //mixins.forEach(s -> AndromedaLog.info("Discovered {}", s));
        return mixins.stream().map(s -> Utilities.supplyUnchecked(() -> MixinService.getService().getBytecodeProvider().getClassNode(s.replace('.', '/'))))
                .filter(MixinProcessor::checkNode).map(n -> n.name.replace('/', '.').substring("me.melontini.andromeda.mixin.".length())).toList();
    }

    public ConfigManager<? extends BasicConfig> getConfig(Class<?> cls) {
        return configs.get(cls);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(Class<T> cls) {
        return (Optional<T>) Optional.ofNullable(modules.get(cls)).map(ModuleInfo::module);
    }
    @SuppressWarnings("unchecked")
    public <T extends Module<?>> Optional<T> getModule(String name) {
        return (Optional<T>) Optional.ofNullable(moduleNames.get(name)).map(ModuleInfo::module);
    }
    public static  <T extends Module<?>> T quick(Class<T> cls) {
        return get().getModule(cls).orElseThrow(() -> new IllegalStateException("Module %s requested quickly, but is not loaded.".formatted(cls)));
    }

    public static ModuleManager get() {
        return INSTANCE.get();
    }

    public static void onClient() {
        get().modules.values().forEach(m -> m.module().onClient());
        AndromedaClient.init();
    }

    public static void onServer() {
        get().modules.values().forEach(m -> m.module().onServer());
    }

    public static void onMain() {
        get().modules.values().forEach(m -> m.module().onMain());
        Andromeda.init();
    }

    public static void onPreLaunch() {
        get().modules.values().forEach(m -> m.module().onPreLaunch());
    }

    public void print() {
        StringBuilder builder = new StringBuilder();
        modules.values().forEach(m -> builder.append(m.name()).append(", "));
        AndromedaLog.info("Loading modules: {}", builder);
    }

    record ModuleInfo(String name, Module<?> module) {

    }
}