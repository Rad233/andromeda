package me.melontini.andromeda.modules.entities.minecarts;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.entities.minecarts.client.Client;
import me.shedaniel.autoconfig.annotation.ConfigEntry;


@Unscoped
@ModuleInfo(name = "minecarts", category = "entities")
public final class Minecarts extends Module {

    public static final ConfigDefinition<Config> MAIN_CONFIG = new ConfigDefinition<>(() -> Config.class);

    Minecarts() {
        this.defineConfig(ConfigState.MAIN, MAIN_CONFIG);
        InitEvent.main(this).listen(() -> () -> {
            MinecartItems.init(this, Andromeda.ROOT_HANDLER.get(MAIN_CONFIG));
            MinecartEntities.init(Andromeda.ROOT_HANDLER.get(MAIN_CONFIG));
        });
        InitEvent.client(this).listen(() -> initClass(Client.class));
    }

    @ToString
    public static final class Config extends BaseConfig {
        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;

        @ConfigEntry.Gui.RequiresRestart
        @SpecialEnvironment(Environment.BOTH)
        public boolean isSpawnerMinecartOn = false;
    }
}
