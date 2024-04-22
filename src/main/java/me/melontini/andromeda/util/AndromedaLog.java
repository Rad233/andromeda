package me.melontini.andromeda.util;

import com.google.common.base.Splitter;
import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.Utilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AndromedaLog {

    private static final boolean prefix = (!Utilities.isDev() && CommonValues.platform() != CommonValues.Platform.CONNECTOR);
    private static final Splitter SPLITTER = Splitter.on(".");

    public static @NotNull PrependingLogger factory() {
        Class<?> cls = Utilities.getCallerClass(2).orElseThrow();
        List<String> split = SPLITTER.splitToList(cls.getName());

        String caller = split.get(split.size() - 1);
        caller = "Andromeda/" + caller;
        if (cls.getName().startsWith("net.minecraft.")) caller += "@Mixin";

        return PrependingLogger.get(caller, logger -> prefix ? "(" + logger.getName() + ") " : "");
    }
}
