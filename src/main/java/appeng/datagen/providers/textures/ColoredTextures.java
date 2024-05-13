/*
 * Copied from Modern Industrialisation (https://github.com/AztechMC/Modern-Industrialization/blob/d537c128c9af90699ff55942da8b6da159222c80/src/client/java/aztech/modern_industrialization/textures/MITextures.java) and adapted for ae2wtlib
 */
/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package appeng.datagen.providers.textures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.Util;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.api.util.AEColor;

public final class ColoredTextures {
    private ColoredTextures() {
    }

    public static CompletableFuture<?> offerTextures(BiConsumer<NativeImage, String> textureWriter,
            ResourceProvider manager, ExistingFileHelper fileHelper) {
        TextureManager mtm = new TextureManager(manager, textureWriter);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        Consumer<IORunnable> defer = r -> futures
                .add(CompletableFuture.runAsync(r::safeRun, Util.backgroundExecutor()));

        for (AEColor color : AEColor.values()) {
            var colorMap = new ColorMap(color);

            defer.accept(() -> generateColoredTexture(mtm, "portable_cell_screen", color, colorMap));
            defer.accept(() -> generateColoredTexture(mtm, "network_tool", color, colorMap));

            defer.accept(() -> generateColoredTexture(mtm, "wireless_terminal", color, colorMap));
            defer.accept(() -> generateColoredTexture(mtm, "wireless_crafting_terminal", color, colorMap));

            defer.accept(() -> generateColoredTexture(mtm, "wireless_terminal_led_lit", color, colorMap));
            defer.accept(() -> generateColoredTexture(mtm, "wireless_terminal_led_unlit", color, colorMap));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> mtm.markTexturesAsGenerated(fileHelper));
    }

    public static void generateColoredTexture(TextureManager mtm, String name, AEColor color, ColorMap colorMap) {
        try {
            NativeImage texture = coloredTexture(mtm, name, colorMap);

            appendTexture(mtm, texture, name + "_" + color.registryPrefix);
            texture.close();
        } catch (Throwable ignored) {
        }
    }

    public static NativeImage coloredTexture(TextureManager mtm, String name, ColorMap colorMap) throws IOException {
        NativeImage image = null;
        String template = "ae2:textures/item/uncolored/" + name + ".png";

        if (mtm.hasAsset(template)) {
            image = mtm.getAssetAsTexture(template);
            colorize(image, colorMap);
        }
        if (image == null)
            throw new RuntimeException("Failed to generate texture for " + name);
        return image;
    }

    public static void colorize(NativeImage image, ColorMap colorramp) {
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                image.setPixelRGBA(i, j, colorramp.map(image.getPixelRGBA(i, j)));
            }
        }
    }

    public static void appendTexture(TextureManager mtm, NativeImage texture, String path)
            throws IOException {
        String texturePath = String.format("ae2:textures/item/%s.png", path);
        mtm.addTexture(texturePath, texture);
        texture.close();
    }
}
