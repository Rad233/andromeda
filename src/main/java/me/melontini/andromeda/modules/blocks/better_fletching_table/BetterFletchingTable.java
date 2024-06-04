package me.melontini.andromeda.modules.blocks.better_fletching_table;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;


@Unscoped
@ModuleInfo(name = "better_fletching_table", category = "blocks")
public final class BetterFletchingTable extends Module {

    BetterFletchingTable() {
        InitEvent.main(this).listen(() -> () -> FletchingScreenHandler.init(this));
        InitEvent.client(this).listen(() -> FletchingScreen::onClient);
    }
}
