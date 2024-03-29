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
 * An image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface CloseableImage {

    /**
     * Gets the color of a pixel in this image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     * @throws IllegalStateException if this image has been closed
     */
    int color(int x, int y);

    /**
     * Sets the color of a pixel in this image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     * @throws IllegalStateException if this image has been closed
     */
    void setColor(int x, int y, int color);

    /**
     * Gets the width (pixels) of this image.
     * @return  the width of this image
     * @throws IllegalStateException if this image has been closed
     */
    int width();

    /**
     * Gets the height (pixels) of this image.
     * @return  the height of this image
     * @throws IllegalStateException if this image has been closed
     */
    int height();

    /**
     * Copies another image's pixels into this image, starting at the top leftmost point (0, 0).
     * The width of the copied area is the smallest of this image's width and the other image's
     * width. Likewise, the height of the copied area is the smallest of this image's height and
     * the other image's height. Pixels outside the copied area in this image are not changed.
     * @param other     the other image to copy data form
     */
    default void copyFrom(CloseableImage other) {
        for (int y = 0; y < Math.min(height(), other.height()); y++) {
            for (int x = 0; x < Math.min(width(), other.width()); x++) {
                setColor(x, y, other.color(x, y));
            }
        }
    }

    /**
     * Uploads the top-left corner of this image at the given coordinates.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     * @throws IllegalStateException if this image has been closed
     */
    void upload(int uploadX, int uploadY);

    /**
     * Takes a portion of this image as a separate {@link CloseableImage}. This image will be
     * closed when the sub-image is closed and vice versa. Changes in the original image or any of
     * its sub-images will be reflected in all the sub-images.
     * @param topLeftX      x-coordinate of the top-left corner of the sub-image
     * @param topLeftY      y-coordinate of the top-left corner of the sub-image
     * @param width         width of the sub-image
     * @param height        height of the sub-image
     * @return the corresponding sub-image
     */
    CloseableImage subImage(int topLeftX, int topLeftY, int width, int height);

    /**
     * <p>Closes any resources associated with this image. Implementations should be idempotent.</p>
     *
     * <p>Currently, no image implementations need to throw exceptions, and {@link AutoCloseable} is not
     * idempotent. An image is not an I/O resource like {@link java.io.Closeable}. Hence, this interface
     * has its own close() method instead of extending one of the existing closeable interfaces.</p>
     */
    void close();

}
