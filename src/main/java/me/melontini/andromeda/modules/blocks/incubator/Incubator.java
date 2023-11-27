package me.melontini.andromeda.modules.blocks.incubator;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.andromeda.base.annotations.ModuleInfo;
import me.melontini.andromeda.base.annotations.ModuleTooltip;
import me.melontini.andromeda.base.config.BasicConfig;
import me.melontini.andromeda.modules.blocks.incubator.data.EggProcessingData;
import me.melontini.andromeda.registries.Common;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@ModuleTooltip(3)
@ModuleInfo(name = "incubator", category = "blocks")
public class Incubator extends Module<Incubator.Config> {

    @Override
    public void onMain() {
        Common.bootstrap(Content.class, EggProcessingData.class);
    }

    @Override
    public Class<Config> configClass() {
        return Config.class;
    }

    public static class Config extends BasicConfig {

        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean randomness = true;
    }
}
