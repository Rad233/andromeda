package me.melontini.andromeda.modules.blocks.campfire_effects;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.config.ScopedConfigs;
import me.melontini.andromeda.common.util.ServerHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class PotionUtil {

    public static @NotNull StatusEffect getStatusEffect(World world, Identifier id) {
        StatusEffect effect = Registries.STATUS_EFFECT.get(id);
        if (effect == null) {
            CampfireEffects m = ModuleManager.quick(CampfireEffects.class);

            ServerHelper.broadcastToOps(requireNonNull(world.getServer()), TextUtil.literal((
                            "(Andromeda) Couldn't get StatusEffect from identifier '%s'.%nReturning 'regeneration' and resetting config ('%s') to default!")
                            .formatted(id, FabricLoader.getInstance().getGameDir().relativize(ScopedConfigs.getPath(world, m))))
                    .formatted(Formatting.RED));

            world.am$get(m).effectList = m.defaultConfig().effectList;
            world.am$save(m);
            return Registries.STATUS_EFFECT.getOrEmpty(Identifier.tryParse("minecraft:regeneration")).orElseThrow();
        }
        return effect;
    }
}
