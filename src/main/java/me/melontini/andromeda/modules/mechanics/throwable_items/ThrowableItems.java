package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.BlockadesEvent;
import me.melontini.andromeda.base.events.ConfigEvent;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.mechanics.throwable_items.client.Client;

import java.util.List;
import java.util.function.BooleanSupplier;

@Unscoped
@ModuleInfo(name = "throwable_items", category = "mechanics")
public class ThrowableItems extends Module<ThrowableItems.Config> {

    ThrowableItems() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));

        BooleanSupplier commander = () -> !Bootstrap.isModLoaded(this, "commander");
        ConfigEvent.forModule(this).listen(manager -> manager.onSave((config, path) -> {
            if (commander.getAsBoolean()) config.enabled = false;
        }));
        BlockadesEvent.BUS.listen(blockade -> blockade.explain(this, "enabled", commander, blockade.andromeda("commander")));
    }

    public static class Config extends BaseConfig {
        @SpecialEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;
        @SpecialEnvironment(Environment.SERVER)
        public int zombieThrowInterval = 40;
        @SpecialEnvironment(Environment.BOTH)
        public boolean tooltip = true;
    }
}
