package me.melontini.andromeda.modules.items.infinite_totem;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.AndromedaItemGroup;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

public class Main {

    public static final Keeper<Item> INFINITE_TOTEM = Keeper.create();
    public static Keeper<DefaultParticleType> KNOCKOFF_TOTEM_PARTICLE = Keeper.create();

    public static final Identifier USED_CUSTOM_TOTEM = new Identifier(MODID, "used_custom_totem");
    public static final Identifier NOTIFY_CLIENT = new Identifier(MODID, "notify_client_about_stuff_please");

    Main(InfiniteTotem module) {
        INFINITE_TOTEM.init(RegistryUtil.register(CommonRegistries.items(), id("infinite_totem"), () -> new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(INFINITE_TOTEM.orThrow()));

        KNOCKOFF_TOTEM_PARTICLE.init(RegistryUtil.register(CommonRegistries.particleTypes(), id("knockoff_totem_particles"), FabricParticleTypes::simple));

        AndromedaItemGroup.accept(acceptor -> acceptor.keeper(module, INFINITE_TOTEM));
    }
}
