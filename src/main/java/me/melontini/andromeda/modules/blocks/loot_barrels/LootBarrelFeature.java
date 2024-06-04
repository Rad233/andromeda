package me.melontini.andromeda.modules.blocks.loot_barrels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class LootBarrelFeature extends Feature<LootBarrelFeature.LootBarrelConfiguration> {

    public LootBarrelFeature() {
        super(LootBarrelConfiguration.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<LootBarrelConfiguration> context) {
        var world = context.getWorld();
        if (!world.getBlockState(context.getOrigin()).isAir() || !world.getBlockState(context.getOrigin().down()).isOpaque()) return false;

        world.setBlockState(context.getOrigin(),
                Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, Direction.random(context.getRandom())),
                2);
        LootableContainerBlockEntity.setLootTable(world, context.getRandom(), context.getOrigin(), context.getConfig().loot());
        return true;
    }

    public record LootBarrelConfiguration(Identifier loot) implements FeatureConfig {

        public static final Codec<LootBarrelConfiguration> CODEC = RecordCodecBuilder.create(data -> data.group(
                Identifier.CODEC.fieldOf("loot").forGetter(LootBarrelConfiguration::loot)
        ).apply(data, LootBarrelConfiguration::new));
    }

    public static void init() {
        Registry.register(Registries.FEATURE, Andromeda.id("loot_barrel"), new LootBarrelFeature());

        RegistryKey<PlacedFeature> key = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Andromeda.id("loot_barrel"));
        BiomeModifications.addFeature(context -> context.canGenerateIn(DimensionOptions.OVERWORLD), GenerationStep.Feature.UNDERGROUND_STRUCTURES, key);
    }
}
