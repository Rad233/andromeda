package me.melontini.andromeda.modules.misc.recipe_advancements_generation;

import com.google.gson.JsonElement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public final class Main {
  private static final Keeper<AdvancementGeneration> MODULE = Keeper.create();
  private static final Map<RecipeType<?>, Function<Context, Return>> RECIPE_TYPE_HANDLERS =
      new HashMap<>();

  public static Function<Context, Return> basicConsumer(
      String typeName, AdvancementGeneration.Config config) {
    return context -> new Return(
        idFromRecipe(context.id(), typeName),
        createAdvBuilder(config, context.id(), context.recipe().getIngredients().get(0)));
  }

  private static Identifier idFromRecipe(Identifier recipe, String typeName) {
    return new Identifier(
        recipe.getNamespace(),
        "recipes/gen/" + typeName + "/" + recipe.toString().replace(":", "_"));
  }

  public static void addRecipeTypeHandler(RecipeType<?> type, Function<Context, Return> consumer) {
    RECIPE_TYPE_HANDLERS.putIfAbsent(type, consumer);
  }

  public static void generateRecipeAdvancements(
      MinecraftServer server, AdvancementGeneration.Config config) {
    AdvancementGeneration module = MODULE.orThrow();
    Map<Identifier, Advancement.Builder> advancementBuilders = new ConcurrentHashMap<>();
    AtomicInteger count = new AtomicInteger();

    List<CompletableFuture<Void>> futures = server.getRecipeManager().values().stream()
        .filter(recipe -> {
          if (config.namespaceBlacklist.contains(recipe.getId().getNamespace())) return false;
          if (config.recipeBlacklist.contains(recipe.getId())) return false;
          if (recipe.isIgnoredInRecipeBook() && config.ignoreRecipesHiddenInTheRecipeBook)
            return false;
          return true;
        })
        .map(recipe -> CompletableFuture.runAsync(
            () -> {
              var handler = RECIPE_TYPE_HANDLERS.get(recipe.getType());
              if (handler != null) {
                count.getAndIncrement();
                var r = handler.apply(new Context(recipe, recipe.getId()));
                if (r != null) advancementBuilders.put(r.id(), r.builder());
              } else {
                if (!recipe.getIngredients().isEmpty()) {
                  count.getAndIncrement();
                  advancementBuilders.put(
                      new Identifier(
                          recipe.getId().getNamespace(),
                          "recipes/gen/generic/" + recipe.getId().toString().replace(":", "_")),
                      createAdvBuilder(
                          config,
                          recipe.getId(),
                          recipe.getIngredients().toArray(Ingredient[]::new)));
                }
              }
            },
            Util.getMainWorkerExecutor()))
        .toList();
    // and?
    CompletableFuture<Void> future =
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    server.runTasks(future::isDone);

    AdvancementManager advancementManager = server.getAdvancementLoader().manager;
    advancementManager.load(advancementBuilders);

    module.logger().info("finished generating {} recipe advancements", count.get());
    advancementBuilders.clear();
  }

  static final class CustomPredicate extends ItemPredicate {
    private final Ingredient ingredient;

    CustomPredicate(Ingredient ingredient) {
      this.ingredient = ingredient;
    }

    @Override
    public boolean test(ItemStack stack) {
      return ingredient.test(stack);
    }

    @Override
    public JsonElement toJson() {
      return ANY.toJson();
    }
  }

  public static @NotNull Advancement.Builder createAdvBuilder(
      AdvancementGeneration.Config config, Identifier id, Ingredient... ingredients) {
    MakeSure.notEmpty(ingredients); // shouldn't really happen
    var builder = Advancement.Builder.createUntelemetered();
    builder.parent(Identifier.of("minecraft", "recipes/root"));

    List<String> names = new ArrayList<>();
    Set<JsonElement> elements = new HashSet<>();
    for (int i = 0; i < ingredients.length; i++) {
      var ingredient = ingredients[i];

      if (ingredient.isEmpty()) continue;
      if (!elements.add(ingredient.toJson())) continue;

      var name = String.valueOf(i);
      names.add(name);
      builder.criterion(
          name, InventoryChangedCriterion.Conditions.items(new CustomPredicate(ingredient)));
    }
    builder.criterion(
        "has_recipe", new RecipeUnlockedCriterion.Conditions(LootContextPredicate.create(), id));

    String[][] reqs;
    if (config.requireAllItems) {
      reqs = new String[names.size()][2];
      for (int i = 0; i < names.size(); i++) {
        String s = names.get(i);
        reqs[i][0] = s;
        reqs[i][1] = "has_recipe";
      }
    } else {
      reqs = new String[1][names.size() + 1];
      for (int i = 0; i < names.size(); i++) {
        String s = names.get(i);
        reqs[0][i] = s;
      }
      reqs[0][names.size()] = "has_recipe";
    }
    builder.requirements(reqs);

    Optional.ofNullable(AdvancementRewards.Builder.recipe(id).build()).ifPresent(builder::rewards);
    return builder;
  }

  static void init(AdvancementGeneration module, AdvancementGeneration.Config config) {
    Main.MODULE.init(module);

    ServerLifecycleEvents.SERVER_STARTING.register(
        server -> generateRecipeAdvancements(server, config));
    BeforeDataPackSyncEvent.EVENT.register(server -> generateRecipeAdvancements(server, config));

    addRecipeTypeHandler(RecipeType.BLASTING, basicConsumer("blasting", config));
    addRecipeTypeHandler(RecipeType.SMOKING, basicConsumer("smoking", config));
    addRecipeTypeHandler(RecipeType.SMELTING, basicConsumer("smelting", config));
    addRecipeTypeHandler(RecipeType.CAMPFIRE_COOKING, basicConsumer("campfire_cooking", config));
    addRecipeTypeHandler(RecipeType.STONECUTTING, basicConsumer("stonecutting", config));
    addRecipeTypeHandler(RecipeType.CRAFTING, (context) -> {
      if (!(context.recipe() instanceof SpecialCraftingRecipe)) {
        if (!context.recipe().getIngredients().isEmpty()) {
          return new Return(
              idFromRecipe(context.id(), "crafting"),
              createAdvBuilder(
                  config,
                  context.id(),
                  context.recipe().getIngredients().toArray(Ingredient[]::new)));
        }
      }
      return null;
    });
  }

  public record Return(Identifier id, Advancement.Builder builder) {}

  public record Context(Recipe<?> recipe, Identifier id) {}
}
