package me.melontini.andromeda.common;//common between modules, not environments.

import com.google.common.collect.Sets;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.config.DataConfigs;
import me.melontini.andromeda.common.registries.Common;
import me.melontini.andromeda.common.util.ServerHelper;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Andromeda {

    public static final Identifier VERIFY_MODULES = new Identifier(MODID, "verify_modules");
    private static Andromeda INSTANCE;

    public static void init() {
        INSTANCE = new Andromeda();
        INSTANCE.onInitialize(ModuleManager.get());
        Support.share("andromeda:main", INSTANCE);
    }

    private void onInitialize(ModuleManager manager) {
        Common.bootstrap();

        ServerLifecycleEvents.SERVER_STARTING.register(ServerHelper::setContext);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerHelper.setContext(null));

        ServerReloadersEvent.EVENT.register(context -> context.register(new DataConfigs()));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var list = manager.loaded().stream().filter(module -> module.config().scope.isDimension()).toList();
            server.getWorlds().forEach(world -> manager.cleanConfigs(server.session.getWorldDirectory(world.getRegistryKey()).resolve("world_config/andromeda"), list));
            manager.cleanConfigs(server.session.getDirectory(WorldSavePath.ROOT).resolve("config/andromeda"),
                    manager.loaded().stream().filter(module -> module.config().scope.isWorld()).toList());
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) DataConfigs.get(server).apply(server);
        });

        if (!AndromedaConfig.get().sideOnlyMode) {
            ServerLoginNetworking.registerGlobalReceiver(VERIFY_MODULES, (server, handler, understood, buf, synchronizer, responseSender) -> {
                if (Debug.Keys.SKIP_SERVER_MODULE_CHECK.isPresent()) return;

                Set<String> modules = manager.loaded().stream()
                        .map(Module::meta).filter(m -> m.environment().isBoth())
                        .map(Module.Metadata::id).collect(Collectors.toSet());
                if (!understood) {
                    if (!modules.isEmpty())
                        handler.disconnect(TextUtil.translatable("andromeda.disconnected.module_mismatch",
                                Arrays.toString(new String[0]), Arrays.toString(modules.toArray())));
                    return;
                }

                int length = buf.readVarInt();
                Set<String> clientModules = new HashSet<>();
                for (int i = 0; i < length; i++) {
                    clientModules.add(buf.readString());
                }

                synchronizer.waitFor(server.submit(() -> {
                    Set<String> disable = Sets.difference(clientModules, modules);
                    Set<String> enable = Sets.difference(modules, clientModules);

                    if (!disable.isEmpty() || !enable.isEmpty()) {
                        handler.disconnect(TextUtil.translatable("andromeda.disconnected.module_mismatch",
                                Arrays.toString(disable.toArray()), Arrays.toString(enable.toArray())));
                    }
                }));
            });
            ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> sender.sendPacket(VERIFY_MODULES, PacketByteBufs.create()));
        }
    }

    @Override
    public String toString() {
        return "Andromeda{version=" + CommonValues.version() + "}";
    }

    public static Andromeda get() {
        return Objects.requireNonNull(INSTANCE, "Andromeda not initialized");
    }

}
