package me.melontini.andromeda.test.server;

import lombok.CustomLog;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.MixinEnvironment;

@CustomLog
public class AndromedaServerTest implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        MutableInt ticks = new MutableInt(0);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks.add(1);

            if (ticks.getValue() >= 200) {
                MixinEnvironment.getCurrentEnvironment().audit();

                server.stop(false);
            }
        });
    }
}
