package me.melontini.andromeda.config;

import lombok.CustomLog;
import me.melontini.andromeda.api.config.TextEntry;
import me.melontini.dark_matter.api.base.util.EntrypointRunner;
import me.melontini.dark_matter.api.config.OptionProcessorRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class FeatureManager {

    public static final String FEATURES_KEY = "andromeda:features";

    public static final BiFunction<String, String, TextEntry> DEFAULT = (s, id) -> TextEntry.translatable(TextEntry.DEFAULT_KEY + id.replace(":", "."));
    public static final Map<String, BiFunction<String, String, TextEntry>> ENTRIES = new HashMap<>();

    static final Map<String, MixinErrorEntry> FAILED_MIXINS = new HashMap<>();
    static final Map<String, ExceptionEntry> UNKNOWN_EXCEPTIONS = new HashMap<>();

    public static void processMixinError(String feature, String className) {
        FAILED_MIXINS.put(feature, new MixinErrorEntry(feature, false, className));
        Config.getManager().save();
    }

    public static void processUnknownException(Throwable t, String... features) {
        for (String feature : features) {
            UNKNOWN_EXCEPTIONS.put(feature, new ExceptionEntry(feature, false, t));
        }
        Config.getManager().save();
    }

    final static ThreadLocal<OptionProcessorRegistry<AndromedaConfig>> REGISTRY = ThreadLocal.withInitial(() -> null);

    static void runLegacy(OptionProcessorRegistry<AndromedaConfig> registry) {
        try {
            REGISTRY.set(registry);
            EntrypointRunner.runEntrypoint("andromeda:feature_manager", Runnable.class, Runnable::run);
        } finally {
            REGISTRY.remove();
        }
    }

    static void legacyRegister(String id, Function<AndromedaConfig, Map<String, Object>> processor) {
        String newId = id.split(":").length == 2 ? id : "legacy:" + id;
        REGISTRY.get().register(newId, manager -> processor.apply(manager.getConfig()));
    }

    static void register(OptionProcessorRegistry<AndromedaConfig> registry, String id, Function<AndromedaConfig, Map<String, Object>> processor) {
        registry.register(id, manager -> processor.apply(manager.getConfig()));
    }

    static void register(OptionProcessorRegistry<AndromedaConfig> registry, String id, Function<AndromedaConfig, Map<String, Object>> processor, BiFunction<String, String, TextEntry> reason) {
        registry.register(id, manager -> processor.apply(manager.getConfig()));
        ENTRIES.put(id, reason);
    }

    record MixinErrorEntry(String feature, Object value, String className) {
    }

    record ExceptionEntry(String feature, Object value, Throwable cause) {
    }
}
