/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Finishes loaded textures lazily with upload components according to the provided {@link Finisher}.
 * @param <I> type of texture builders (input)
 * @param <O> type of textures (output)
 * @author soir20
 */
public class LazyTextureManager<I, O extends AbstractTexture & CustomTickable> implements Manager<I> {
    private final Manager<? super AbstractTexture> DELEGATE;
    private final Map<ResourceLocation, CustomTickable> TICKABLE_TEXTURES;
    private final Finisher<? super I, ? extends O> FINISHER;

    /**
     * Creates the TextureManagerWrapper.
     * @param delegate      Minecraft's the texture manager
     * @param finisher      lazily finishes textures once resource loading is complete
     */
    public LazyTextureManager(Manager<? super AbstractTexture> delegate,
                              Finisher<? super I, ? extends O> finisher) {
        DELEGATE = requireNonNull(delegate, "Delegate manager cannot be null");
        TICKABLE_TEXTURES = new HashMap<>();
        FINISHER = requireNonNull(finisher, "Finisher cannot be null");
    }

    @Override
    public void register(ResourceLocation textureLocation, I builder) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(builder, "Texture builder cannot be null");

        /* Clear any existing texture immediately to prevent PreloadedTextures
           from re-adding themselves. The texture manager will reload before the
           EventDrivenTextures are added, causing a race condition with the
           registration CompletableFuture inside PreloadedTexture's reset method. */
        DELEGATE.unregister(textureLocation);

        FINISHER.queue(textureLocation, builder);
    }

    /**
     * Finishes all queued textures by adding them to Minecraft's texture manager
     * according to the provided {@link Finisher}.
     */
    public void finishQueued() {
        Map<ResourceLocation, ? extends O> textures = FINISHER.finish();

        textures.forEach((location, texture) -> {
            DELEGATE.register(location, texture);
            TICKABLE_TEXTURES.put(location, texture);
        });
    }

    @Override
    public void unregister(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");

        DELEGATE.unregister(textureLocation);
        TICKABLE_TEXTURES.remove(textureLocation);
    }

    @Override
    public void tick() {
        TICKABLE_TEXTURES.values().forEach(CustomTickable::tick);
    }

}
