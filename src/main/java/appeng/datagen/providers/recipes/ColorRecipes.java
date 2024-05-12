package appeng.datagen.providers.recipes;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.AEBaseItem;
import appeng.recipes.handlers.ColorRecipe;

public class ColorRecipes extends AE2RecipeProvider {
    public ColorRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        for (var color : AEColor.values()) {
            color(recipeOutput, AEItems.WIRELESS_CRAFTING_TERMINAL.asItem(), color);
            color(recipeOutput, AEItems.WIRELESS_TERMINAL.asItem(), color);

            color(recipeOutput, AEItems.PORTABLE_ITEM_CELL1K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_ITEM_CELL4K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_ITEM_CELL16K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_ITEM_CELL64K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_ITEM_CELL256K.asItem(), color);

            color(recipeOutput, AEItems.PORTABLE_FLUID_CELL1K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_FLUID_CELL4K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_FLUID_CELL16K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_FLUID_CELL64K.asItem(), color);
            color(recipeOutput, AEItems.PORTABLE_FLUID_CELL256K.asItem(), color);

            color(recipeOutput, AEItems.NETWORK_TOOL.asItem(), color);
        }
    }

    private static void color(RecipeOutput recipeOutput, AEBaseItem item, AEColor color) {
        TagKey<Item> dye;
        if (color == AEColor.TRANSPARENT) {
            dye = ConventionTags.CAN_REMOVE_COLOR;
        } else
            dye = color.dye.getTag();
        recipeOutput.accept(
                AppEng.makeId("color/" + Objects.requireNonNull(item.getRegistryName()).getPath() + "_"
                        + color.registryPrefix),
                new ColorRecipe(Ingredient.of(item), Ingredient.of(dye), color,
                        new ItemStack(item)),
                null);
    }

    @Override
    public String getName() {
        return "AE2 Coloring Recipes";
    }
}
