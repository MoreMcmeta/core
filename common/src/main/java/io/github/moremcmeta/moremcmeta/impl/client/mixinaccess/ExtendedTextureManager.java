/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2025 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.mixinaccess;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Extends the vanilla {@link net.minecraft.client.renderer.texture.TextureManager}.
 * @author soir20
 */
public interface ExtendedTextureManager {

    /**
     * Checks if this texture manager contains a texture with the given location.
     * @param location      location of the texture
     * @return whether this texture manager contains a texture with the given location
     */
    boolean contains(ResourceLocation location);

    /**
     * Retrieves the texture with the given location, if this manager contains one.
     * @param location      location of the texture
     * @return the texture at the given location, if any
     */
    Optional<AbstractTexture> texture(ResourceLocation location);

}
