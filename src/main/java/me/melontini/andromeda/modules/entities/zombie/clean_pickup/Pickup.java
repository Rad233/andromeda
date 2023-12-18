package me.melontini.andromeda.modules.entities.zombie.clean_pickup;

import com.google.gson.JsonObject;
import me.melontini.andromeda.base.BasicModule;
import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.common.registries.Common;

@ModuleTooltip(3)
@ModuleInfo(name = "zombie/clean_pickup", category = "entities", environment = Environment.SERVER)
public class Pickup extends BasicModule {

    @Override
    public void acceptLegacyConfig(JsonObject config) {
        if (config.has("newThrowableItems")) {
            JsonObject o = config.get("newThrowableItems").getAsJsonObject();
            this.config().enabled = o.has("enable") && o.get("enable").getAsBoolean()
                    && o.has("preventUselessItems") && o.get("preventUselessItems").getAsBoolean();
        }
    }

    @Override
    public void onMain() {
        Common.bootstrap(this, PickupTag.class);
    }
}
