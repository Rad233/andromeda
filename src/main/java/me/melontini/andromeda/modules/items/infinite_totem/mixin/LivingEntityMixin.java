package me.melontini.andromeda.modules.items.infinite_totem.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.items.infinite_totem.InfiniteTotem;
import me.melontini.andromeda.modules.items.infinite_totem.Main;
import me.melontini.dark_matter.api.minecraft.util.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean andromeda$infiniteFallback(boolean original, DamageSource source, @Local(index = 3) ItemStack itemStack) {
        return original || (world.am$get(InfiniteTotem.CONFIG).available.asBoolean(LootContextUtil.fishing(world, this.getPos(), itemStack)) && itemStack.isOf(Main.INFINITE_TOTEM.orThrow()));
    }

    @WrapWithCondition(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private boolean andromeda$infiniteFallback(ItemStack instance, int i) {
        return !instance.isOf(Main.INFINITE_TOTEM.orThrow());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V", shift = At.Shift.BEFORE), method = "tryUseTotem", cancellable = true)
    private void andromeda$useInfiniteTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack itemStack) {
        if (itemStack.isOf(Main.INFINITE_TOTEM.orThrow())) {
            if (!world.isClient()) {
                PacketByteBuf buf = PacketByteBufs.create()
                        .writeUuid(this.getUuid())
                        .writeItemStack(new ItemStack(Main.INFINITE_TOTEM.orThrow()));
                buf.writeRegistryValue(Registries.PARTICLE_TYPE, Main.KNOCKOFF_TOTEM_PARTICLE.orThrow());

                for (PlayerEntity player : PlayerUtil.findPlayersInRange(world, getBlockPos(), 120)) {
                    ServerPlayNetworking.send((ServerPlayerEntity) player, Main.USED_CUSTOM_TOTEM, buf);
                }
            }
            cir.setReturnValue(true);
        }
    }
}
