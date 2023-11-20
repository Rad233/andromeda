package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.registries.Keeper;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.registries.Common.start;

public class Content {

    public static final Keeper<LockpickItem> LOCKPICK = start(() -> ContentBuilder.ItemBuilder
            .create(id("lockpick"), () -> new LockpickItem(new FabricItemSettings().maxCount(16)))
            .itemGroup(ItemGroup.TOOLS)
            .register(() -> ModuleManager.quick(Lockpick.class).config().enabled));

    public static final Keeper<ScreenHandlerType<MerchantInventoryScreenHandler>> MERCHANT_INVENTORY = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(() -> ModuleManager.quick(Lockpick.class).config().villagerInventory,
                    id("merchant_inventory"), () -> MerchantInventoryScreenHandler::new));
}