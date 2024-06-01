package me.melontini.andromeda.modules.mechanics.throwable_items.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.modules.mechanics.throwable_items.Main;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

@Mixin(DispenserBlock.class)
abstract class DispenserBlockMixin {

    @Inject(at = @At("TAIL"), method = "method_10008")
    private static void andromeda$throwItem(Object2ObjectOpenHashMap<Item, DispenserBehavior> map, CallbackInfo ci) {
        var b = map.defaultReturnValue();
        map.defaultReturnValue((pointer, stack) -> pointer.getWorld().getServer().dm$getReloader(RELOADER).hasBehaviors(stack) ?
                Main.BEHAVIOR.dispense(pointer, stack) : b.dispense(pointer, stack));
    }

    @Inject(at = @At("HEAD"), method = "getBehaviorForItem", cancellable = true)
    private void andromeda$overrideBehavior(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> cir) {
        var server = Andromeda.get().getCurrentServer();
        if (server == null) return;

        var manager = server.dm$getReloader(RELOADER);
        if (manager.hasBehaviors(stack) && manager.overridesVanilla(stack.getItem())) {
            cir.setReturnValue(Main.BEHAVIOR);
        }
    }
}
