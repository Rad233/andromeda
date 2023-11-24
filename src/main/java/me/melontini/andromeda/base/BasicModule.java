package me.melontini.andromeda.base;

import me.melontini.andromeda.base.config.BasicConfig;

public abstract class BasicModule extends Module<BasicConfig> {

    @Override
    public final Class<BasicConfig> configClass() {
        return BasicConfig.class;
    }
}
