package me.melontini.andromeda.entity.vehicle.boats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;


//Ctrl + c, Ctrl + v
public abstract class StorageBoatEntity extends BoatEntityWithBlock implements RideableInventory, VehicleInventory {
    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    @Nullable
    public Identifier lootTableId;
    public long lootSeed;

    public StorageBoatEntity(EntityType<? extends BoatEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        this.generateLoot(null);
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.generateLoot(null);
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.generateLoot(null);
        ItemStack itemStack = this.inventory.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.inventory.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.generateLoot(null);
        this.inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

    }

    @Override
    public StackReference getStackReference(int mappedIndex) {
        return mappedIndex >= 0 && mappedIndex < this.size() ? new StackReference() {
            @Override
            public ItemStack get() {
                return StorageBoatEntity.this.getStack(mappedIndex);
            }

            @Override
            public boolean set(ItemStack stack) {
                StorageBoatEntity.this.setStack(mappedIndex, stack);
                return true;
            }
        } : super.getStackReference(mappedIndex);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!player.shouldCancelInteraction() && this.getPassengerList().isEmpty()) {
            super.interact(player, hand);
        } else {
            player.openHandledScreen(this);
            if (!player.world.isClient) {
                this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinBrain.onGuardedBlockInteracted(player, true);
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.isRemoved()) {
            return false;
        } else {
            return !(player.squaredDistanceTo(this) > 64.0);
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.world.isClient && reason.shouldDestroy()) {
            ItemScatterer.spawn(this.world, this, this);
        }

        super.remove(reason);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (this.lootTableId != null && playerEntity.isSpectator()) {
            return null;
        } else {
            this.generateLoot(playerInventory.player);
            return this.getScreenHandler(i, playerInventory);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.lootTableId != null) {
            nbt.putString("LootTable", this.lootTableId.toString());
            if (this.lootSeed != 0L) {
                nbt.putLong("LootTableSeed", this.lootSeed);
            }
        } else {
            Inventories.writeNbt(nbt, this.inventory);
        }

    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (nbt.contains("LootTable", 8)) {
            this.lootTableId = new Identifier(nbt.getString("LootTable"));
            this.lootSeed = nbt.getLong("LootTableSeed");
        } else {
            Inventories.readNbt(nbt, this.inventory);
        }

    }

    @Override
    public void clear() {
        this.generateLoot(null);
        this.inventory.clear();
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public void resetInventory() {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
    }

    public void setLootTableId(@Nullable Identifier lootTableId) {
        this.lootTableId = lootTableId;
    }

    @Nullable
    @Override
    public Identifier getLootTableId() {
        return lootTableId;
    }

    @Override
    public long getLootTableSeed() {
        return lootSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootSeed = lootTableSeed;
    }

    public void generateLoot(@Nullable PlayerEntity player) {
        this.generateInventoryLoot(player);
    }

    @Override
    public void openInventory(PlayerEntity player) {
        player.openHandledScreen(this);
        if (!player.world.isClient) {
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(player, true);
        }
    }

    public abstract ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory);
}
