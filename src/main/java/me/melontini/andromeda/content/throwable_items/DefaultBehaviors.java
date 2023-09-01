package me.melontini.andromeda.content.throwable_items;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.networks.AndromedaPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DefaultBehaviors implements Runnable {

    private static final Set<Item> DYE_ITEMS = Set.of(
            Items.RED_DYE, Items.BLUE_DYE, Items.LIGHT_BLUE_DYE,
            Items.CYAN_DYE, Items.BLACK_DYE, Items.BROWN_DYE,
            Items.GREEN_DYE, Items.PINK_DYE,  Items.PURPLE_DYE,
            Items.YELLOW_DYE, Items.WHITE_DYE, Items.ORANGE_DYE,
            Items.LIME_DYE, Items.MAGENTA_DYE, Items.LIGHT_GRAY_DYE,
            Items.GRAY_DYE);

    @Override
    public void run() {
        ItemBehaviorManager.addBehavior(Items.BONE_MEAL, (stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult result = (BlockHitResult) hitResult;
                BlockPos blockPos = result.getBlockPos();
                BlockPos blockPos2 = blockPos.offset(result.getSide());

                if (BoneMealItem.useOnFertilizable(stack, world, blockPos)) {
                    world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
                } else {
                    BlockState blockState = world.getBlockState(blockPos);
                    boolean bl = blockState.isSideSolidFullSquare(world, blockPos, result.getSide());
                    if (bl && BoneMealItem.useOnGround(stack, world, blockPos2, result.getSide())) {
                        world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos2, 0);
                    }
                }
            }
        });

        ItemBehaviorManager.addBehavior(Items.INK_SAC, (stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient) {
                addEffects(hitResult, world, user, new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            }
        });
        ItemBehaviorManager.addBehavior(Items.GLOW_INK_SAC, (stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient) {
                addEffects(hitResult, world, user, new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0), new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
            }
        });

        for (Item item : DYE_ITEMS) {
            ItemBehaviorManager.addBehavior(item, (stack, flyingItemEntity, world, user, hitResult) -> {
                if (!world.isClient) {
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                        if (entityHitResult.getEntity() instanceof PlayerEntity player) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeItemStack(stack);

                            ServerPlayNetworking.send((ServerPlayerEntity) player, AndromedaPackets.COLORED_FLYING_STACK_LANDED, buf);
                        } else {
                            Vec3d pos = hitResult.getPos();
                            List<PlayerEntity> playerEntities = world.getEntitiesByClass(PlayerEntity.class, new Box(new BlockPos(pos)).expand(0.5), LivingEntity::isAlive);
                            playerEntities.stream().min(Comparator.comparingDouble(player -> player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())))
                                    .ifPresent(player -> {
                                        PacketByteBuf buf = PacketByteBufs.create();
                                        buf.writeItemStack(stack);

                                        ServerPlayNetworking.send((ServerPlayerEntity) player, AndromedaPackets.COLORED_FLYING_STACK_LANDED, buf);
                                    });
                        }
                    } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                        Vec3d pos = hitResult.getPos();
                        List<PlayerEntity> playerEntities = world.getEntitiesByClass(PlayerEntity.class, new Box(((BlockHitResult) hitResult).getBlockPos()).expand(0.5), LivingEntity::isAlive);
                        playerEntities.stream().min(Comparator.comparingDouble(player -> player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())))
                                .ifPresent(player -> {
                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeItemStack(stack);

                                    ServerPlayNetworking.send((ServerPlayerEntity) player, AndromedaPackets.COLORED_FLYING_STACK_LANDED, buf);
                                });
                    }
                }
            });
        }

        ItemBehaviorManager.addBehaviors((stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient) {
                if (hitResult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) hitResult).getEntity();
                    entity.damage(Andromeda.bricked(user), 2);
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
                    }
                    if (entity instanceof Angerable angerable && user instanceof LivingEntity livingEntity) {
                        angerable.setTarget(livingEntity);
                    }
                }
                world.spawnEntity(new ItemEntity(world, flyingItemEntity.getX(), flyingItemEntity.getY(), flyingItemEntity.getZ(), stack));
            }
        }, Items.BRICK, Items.NETHER_BRICK);

        ItemBehaviorManager.addBehavior(Items.FIRE_CHARGE, (stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient) {
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult result = (BlockHitResult) hitResult;
                    BlockPos blockPos = result.getBlockPos();
                    BlockState blockState = world.getBlockState(blockPos);

                    if (blockState.getBlock() instanceof TntBlock) {
                        TntBlock.primeTnt(world, blockPos);
                        world.removeBlock(blockPos, false);
                    } else if (FlammableBlockRegistry.getDefaultInstance().get(blockState.getBlock()) != null) {
                        world.setBlockState(blockPos.offset(result.getSide()), FireBlock.getState(world, blockPos.offset(result.getSide())));
                    }
                    Random random = world.getRandom();
                    world.playSound(null, flyingItemEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult result = (EntityHitResult) hitResult;
                    Entity entity = result.getEntity();
                    if (entity instanceof LivingEntity livingEntity) livingEntity.takeKnockback(0.4, -flyingItemEntity.getVelocity().getX(), -flyingItemEntity.getVelocity().getZ());
                    entity.setOnFireFor(8);
                    Random random = world.getRandom();
                    world.playSound(null, flyingItemEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                }
            }
        });

        ItemBehaviorManager.addBehavior(Items.GUNPOWDER, (stack, flyingItemEntity, world, user, hitResult) -> {
            if (!world.isClient) {
                world.createExplosion(user, flyingItemEntity.getX(), flyingItemEntity.getY(), flyingItemEntity.getZ(), 1, Explosion.DestructionType.BREAK);
            }
        });
    }

    public static void addEffects(HitResult hitResult, World world, Entity user, StatusEffectInstance... instances) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Vec3d pos = hitResult.getPos();
            List<LivingEntity> livingEntities = world.getEntitiesByClass(LivingEntity.class, new Box(((BlockHitResult) hitResult).getBlockPos()).expand(0.5), LivingEntity::isAlive);
            livingEntities.stream().min(Comparator.comparingDouble(livingEntity -> livingEntity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())))
                    .ifPresent(livingEntity -> {
                        for (StatusEffectInstance instance : instances) {
                            livingEntity.addStatusEffect(instance);
                        }
                    });
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                for (StatusEffectInstance instance : instances) {
                    livingEntity.addStatusEffect(instance, user);
                }
            } else {
                Vec3d pos = hitResult.getPos();
                List<LivingEntity> livingEntities = world.getEntitiesByClass(LivingEntity.class, new Box(new BlockPos(pos)).expand(0.5), LivingEntity::isAlive);
                livingEntities.stream().min(Comparator.comparingDouble(livingEntity -> livingEntity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())))
                        .ifPresent(livingEntity -> {
                            for (StatusEffectInstance instance : instances) {
                                livingEntity.addStatusEffect(instance);
                            }
                        });
            }
        }
    }

}