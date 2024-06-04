package me.melontini.andromeda.modules.blocks.loot_barrels;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "loot_barrels", category = "blocks", environment = Environment.SERVER)
public final class LootBarrels extends Module {

    LootBarrels() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
    }
}
