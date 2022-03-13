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

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.api.Image;

/**
 * An image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface CloseableImage extends Image {

    /**
     * Gets the color of a pixel in this image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     * @throws IllegalStateException if this image has been closed
     */
    int getPixel(int x, int y);

    /**
     * Sets the color of a pixel in this image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     * @throws IllegalStateException if this image has been closed
     */
    void setPixel(int x, int y, int color);

    /**
     * Gets the width (pixels) of this image.
     * @return  the width of this image
     * @throws IllegalStateException if this image has been closed
     */
    int getWidth();

    /**
     * Gets the height (pixels) of this image.
     * @return  the height of this image
     * @throws IllegalStateException if this image has been closed
     */
    int getHeight();

    /**
     * Gets the visible area (iterable by point) of this image. Order of points is not guaranteed.
     * @return  the visible area of this image
     * @throws IllegalStateException if this image has been closed
     */
    Image.VisibleArea getVisibleArea();

    /**
     * Uploads the top-left corner of this image at the given coordinates.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     * @throws IllegalStateException if this image has been closed
     */
    void upload(int uploadX, int uploadY);

    /**
     * Closes any resources associated with this image. Implementations should be idempotent.
     *
     * Currently, no image implementations need to throw exceptions, and {@link AutoCloseable} is not
     * idempotent. An image is not an I/O resource like {@link java.io.Closeable}. Hence, this interface
     * has its own close() method instead of extending one of the existing closeable interfaces.
     */
    void close();

}
