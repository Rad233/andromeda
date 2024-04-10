package me.melontini.andromeda.common.conflicts;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class CommonRegistries {

    public static DefaultedRegistry<Item> items() {
        return Registries.ITEM;
    }

    public static DefaultedRegistry<Block> blocks() {
        return Registries.BLOCK;
    }

    public static DefaultedRegistry<EntityType<?>> entityTypes() {
        return Registries.ENTITY_TYPE;
    }

    public static Registry<BlockEntityType<?>> blockEntityTypes() {
        return Registries.BLOCK_ENTITY_TYPE;
    }

    public static Registry<ParticleType<?>> particleTypes() {
        return Registries.PARTICLE_TYPE;
    }

    public static Registry<StatusEffect> statusEffects() {
        return Registries.STATUS_EFFECT;
    }

    public static Registry<ScreenHandlerType<?>> screenHandlers() {
        return Registries.SCREEN_HANDLER;
    }
}
