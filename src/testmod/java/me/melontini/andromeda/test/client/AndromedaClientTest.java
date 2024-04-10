package me.melontini.andromeda.test.client;

import lombok.CustomLog;
import me.melontini.dark_matter.api.minecraft.client.events.AfterFirstReload;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.MixinEnvironment;

import static me.melontini.andromeda.test.client.FabricClientTestHelper.waitForWorldTicks;

@CustomLog
public class AndromedaClientTest implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AfterFirstReload.EVENT.register(() -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (!client.getLevelStorage().levelExists("andromeda_test_world")) {
                client.createIntegratedServerLoader().createAndStart("andromeda_test_world",
                        new LevelInfo("andromeda_test_world", GameMode.CREATIVE, false, Difficulty.EASY, true,
                                new GameRules(), DataConfiguration.SAFE_MODE),
                        new GeneratorOptions(0, true, false),
                        registryManager -> registryManager.get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value().createDimensionsRegistryHolder());
            } else {
                client.createIntegratedServerLoader().start(new TitleScreen(), "andromeda_test_world");
            }
        });

        var thread = new Thread(() -> {
            try {
                LOGGER.info("Started client test.");
                waitForWorldTicks(200);

                MixinEnvironment.getCurrentEnvironment().audit();

                MinecraftClient.getInstance().scheduleStop();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });
        thread.start();
    }
}
