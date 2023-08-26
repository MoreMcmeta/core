/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Wraps Minecraft's {@link net.minecraft.client.renderer.texture.TextureManager} to fix a bug with
 * tracking {@link net.minecraft.client.renderer.texture.Tickable} textures.
 * @param <T> texture type
 * @author soir20
 */
public final class TextureManagerWrapper<T extends AbstractTexture & CustomTickable> implements Manager<T> {
    private final Manager<? super AbstractTexture> DELEGATE;
    private final Map<ResourceLocation, CustomTickable> TICKABLE_TEXTURES;

    /**
     * Creates the TextureManagerWrapper.
     * @param delegate      Minecraft's the texture manager
     */
    public TextureManagerWrapper(Manager<? super AbstractTexture> delegate) {
        DELEGATE = requireNonNull(delegate, "Delegate manager cannot be null");
        TICKABLE_TEXTURES = new ConcurrentHashMap<>();
    }

    @Override
    public void register(ResourceLocation textureLocation, T texture) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(texture, "Texture cannot be null");

        TICKABLE_TEXTURES.put(textureLocation, texture);
        DELEGATE.register(textureLocation, texture);
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
