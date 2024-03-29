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

/**
 * Prepares a texture in OpenGL. Textures must already have an initialized ID, so
 * an {@link net.minecraft.client.renderer.texture.AbstractTexture} should be created
 * first.
 * @author soir20
 */
@FunctionalInterface
public interface TexturePreparer {

    /**
     * Prepares a new texture in OpenGL.
     * @param glId          the unique ID of the new texture in OpenGL
     * @param mipmapLevel   the mipmap level of the texture
     * @param width         the width of the texture (no mipmap)
     * @param height        the height of the texture (no mipmap)
     */
    void prepare(int glId, int mipmapLevel, int width, int height);

}
