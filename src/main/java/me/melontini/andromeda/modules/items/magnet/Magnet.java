package me.melontini.andromeda.modules.items.magnet;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.ConfigDefinition;
import me.melontini.andromeda.base.util.ConfigState;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

import java.util.List;

@Unscoped
@ModuleInfo(name = "magnet", category = "items")
public final class Magnet extends Module {

    public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

    Magnet() {
        this.defineConfig(ConfigState.GAME, CONFIG);
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }

    @ToString
    public static class Config extends BaseConfig {
        public DoubleIntermediary rangeMultiplier = DoubleIntermediary.of(5);
    }
}
