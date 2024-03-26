package me.melontini.andromeda.modules.mechanics.throwable_items.data;

import com.mojang.serialization.Codec;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import me.melontini.commander.command.Command;
import me.melontini.commander.command.CommandType;
import me.melontini.commander.command.selector.ConditionedSelector;
import me.melontini.commander.event.EventContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public record ItemPlopEffect(ConditionedSelector selector) implements Command {

    public static final Codec<ItemPlopEffect> CODEC = ConditionedSelector.CODEC.fieldOf("selector").xmap(ItemPlopEffect::new, ItemPlopEffect::selector).codec();

    @Override
    public boolean execute(EventContext context) {
        var opt = selector.select(context);
        if (opt.isEmpty()) return false;
        Entity entity = opt.get().getEntity();
        if (entity instanceof ServerPlayerEntity player) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeItemStack(context.lootContext().get(LootContextParameters.TOOL));
            ServerPlayNetworking.send(player, Main.COLORED_FLYING_STACK_LANDED, buf);
        } else if (entity instanceof LivingEntity living){
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false, true));
        }
        return true;
    }

    @Override
    public CommandType type() {
        return Main.ITEM_PLOP_COMMAND.orThrow();
    }
}
