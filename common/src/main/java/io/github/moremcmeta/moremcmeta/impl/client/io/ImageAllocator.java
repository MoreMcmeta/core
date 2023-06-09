/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import io.github.moremcmeta.moremcmeta.impl.client.texture.CloseableImage;

/**
 * Allocates an image. The resulting image should be entirely transparent with
 * all pixels having value 0.
 * @author soir20
 */
public interface ImageAllocator {

    /**
     * Allocates a new, transparent image.
     * @param width         the width of the image to allocate
     * @param height        the height of the image to allocate
     * @param mipmapLevel   the number mipmap that this image represents
     * @param blur          whether to blur the image
     * @param clamp         whether to clamp the image
     * @return the allocated image
     */
    CloseableImage allocate(int width, int height, int mipmapLevel, boolean blur, boolean clamp);

}
