package appeng.recipes.handlers;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ColorRecipeSerializer implements net.minecraft.world.item.crafting.RecipeSerializer<ColorRecipe> {
    public static final ColorRecipeSerializer INSTANCE = new ColorRecipeSerializer();

    @Override
    public MapCodec<ColorRecipe> codec() {
        return ColorRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ColorRecipe> streamCodec() {
        return ColorRecipe.STREAM_CODEC;
    }
}
