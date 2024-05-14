package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import appeng.api.ids.AEComponents;
import appeng.api.util.AEColor;
import appeng.core.AppEng;

public record ColorRecipe(Ingredient input, Ingredient dye, AEColor color, ItemStack output) implements CraftingRecipe {

    public static final MapCodec<ColorRecipe> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(ColorRecipe::input),
                    Ingredient.CODEC.fieldOf("dye").forGetter(ColorRecipe::dye),
                    AEColor.CODEC.fieldOf("color")
                            .forGetter(ColorRecipe::color),
                    ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("output")
                            .forGetter(ColorRecipe::output))
                    .apply(builder, ColorRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ColorRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ColorRecipe::input,
            Ingredient.CONTENTS_STREAM_CODEC, ColorRecipe::dye,
            AEColor.STREAM_CODEC, ColorRecipe::color,
            ItemStack.STREAM_CODEC, ColorRecipe::output,
            ColorRecipe::new);
    public static final ResourceLocation TYPE_ID = AppEng.makeId("color");

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        int items = 0;
        for (var item : inv.getItems()) {
            if (!item.isEmpty()) {
                items++;
            }
        }
        return inv.hasAnyMatching(input) && inv.hasAnyMatching(dye) && items == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider registries) {
        for (ItemStack stack : inv.getItems()) {
            if (input.test(stack)) {
                stack = stack.copy();
                stack.set(AEComponents.COLOR, color);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        var result = output.copy();
        result.set(AEComponents.COLOR, color);
        return result;
    }

    @Override
    public RecipeSerializer<ColorRecipe> getSerializer() {
        return ColorRecipeSerializer.INSTANCE;
    }
}
