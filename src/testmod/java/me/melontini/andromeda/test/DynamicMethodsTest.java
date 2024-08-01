package me.melontini.andromeda.test;

import com.mojang.logging.LogUtils;
import me.melontini.andromeda.modules.items.lockpick.Lockpick;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class DynamicMethodsTest implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Lockpick.Config cfg = new Lockpick.Config();

        LOGGER.info(cfg.toString());
        Lockpick.Config cloned = (Lockpick.Config) cfg.copy();
        LOGGER.info("Equals {}", cfg.equals(cloned));
        cloned.chance = LongIntermediary.of(4);
        LOGGER.info("Equals {}", cfg.equals(cloned));
    }
}
