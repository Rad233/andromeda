package me.melontini.andromeda.modules.entities.villagers_follow_emeralds;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import static me.melontini.andromeda.common.Andromeda.id;

public class VillagerTemptGoal extends TemptGoal {

    public static final TagKey<Item> TEMPTING = TagKey.of(Registries.ITEM.getKey(), id("tempting_for_villagers"));

    public VillagerTemptGoal(VillagerEntity entity, double speed, Ingredient food, boolean canBeScared) {
        super(entity, speed, food, canBeScared);
    }

    @Override
    public boolean canStart() {
        if (this.mob.world.isClient()) return false;
        if (!this.mob.world.am$get(VillagersFollowEmeralds.class).enabled) return false;

        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        } else {
            if (mob.getBrain().hasActivity(Activity.PANIC) || mob.getBrain().hasActivity(Activity.REST) || mob.getBrain().hasActivity(Activity.HIDE)) {
                return false;
            } else {
                this.closestPlayer = this.mob.getWorld().getClosestPlayer(this.predicate, this.mob);
                return closestPlayer != null;
            }
        }
    }
}
