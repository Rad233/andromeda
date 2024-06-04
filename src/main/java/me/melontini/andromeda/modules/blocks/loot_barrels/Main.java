package me.melontini.andromeda.modules.blocks.loot_barrels;

import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class Main {

    Main() {
        Registry.register(Registries.FEATURE, Andromeda.id("loot_barrel"), new LootBarrelFeature());

        RegistryKey<PlacedFeature> key = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Andromeda.id("loot_barrel"));
        BiomeModifications.addFeature(context -> context.canGenerateIn(DimensionOptions.OVERWORLD), GenerationStep.Feature.UNDERGROUND_STRUCTURES, key);
    }
}
