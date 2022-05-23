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

package io.github.soir20.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.io.FrameReader;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link CloseableImage}.
 * @author soir20
 */
public class CloseableImageFrame {
    private final int WIDTH;
    private final int HEIGHT;
    private ImmutableList<? extends CloseableImage> mipmaps;
    private boolean closed;

    /**
     * Creates a new frame based on frame data.
     * @param frameData             general data associated with the frame
     * @param mipmaps               mipmapped images for this frame (starting with the original image)
     */
    public CloseableImageFrame(FrameReader.FrameData frameData, ImmutableList<? extends CloseableImage> mipmaps) {
        requireNonNull(frameData, "Frame data cannot be null");
        this.mipmaps = requireNonNull(mipmaps, "Mipmaps cannot be null");
        if (mipmaps.isEmpty()) {
            throw new IllegalArgumentException("At least one mipmap must be provided");
        }

        int frameWidth = frameData.getWidth();
        int frameHeight = frameData.getHeight();

        for (int level = 0; level < mipmaps.size(); level++) {
            CloseableImage image = mipmaps.get(level);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            if (imageWidth != frameWidth >> level || imageHeight != frameHeight >> level) {
                throw new IllegalArgumentException(String.format(
                        "Mipmap %s of size %sx%s conflicts with frame size %sx%s",
                        level, imageWidth, imageHeight, frameWidth, frameHeight
                ));
            }
        }

        WIDTH = frameWidth;
        HEIGHT = frameHeight;
        closed = false;
    }

    /**
     * Gets the color of the given pixel in the top-level mipmap of this frame.
     * @param x     x coordinate of the pixel (from the top left)
     * @param y     y coordinate of the pixel (from the top left)
     * @return the color of the pixel at the given coordinate
     * @throws IllegalStateException if this frame has been closed
     */
    public int color(int x, int y) {
        checkOpen();
        return mipmaps.get(0).getPixel(x, y);
    }

    /**
     * Uploads this frame at a given position in the active texture.
     * @param point     point to upload the top-left corner of this frame at
     * @throws IllegalStateException if this frame has been closed
     */
    public void uploadAt(Point point) {
        checkOpen();

        requireNonNull(point, "Point cannot be null");

        if (point.getX() < 0 || point.getY() < 0) {
            throw new IllegalArgumentException("Point coordinates must be greater than zero");
        }

        for (int level = 0; level < mipmaps.size(); level++) {
            int mipmappedX = point.getX() >> level;
            int mipmappedY = point.getY() >> level;

            CloseableImage mipmap = mipmaps.get(level);
            int mipmappedWidth = mipmap.getWidth();
            int mipmappedHeight = mipmap.getHeight();

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                mipmap.upload(mipmappedX, mipmappedY);
            }
        }
    }

    /**
     * Gets the width of this frame in pixels.
     * @return  the width of this frame in pixels
     * @throws IllegalStateException if this frame has been closed
     */
    public int getWidth() {
        checkOpen();
        return WIDTH;
    }

    /**
     * Gets the height of this frame in pixels.
     * @return  the height of this frame in pixels
     * @throws IllegalStateException if this frame has been closed
     */
    public int getHeight() {
        checkOpen();
        return HEIGHT;
    }

    /**
     * Gets the mipmap level of this frame.
     * @return the mipmap level of this frame
     * @throws IllegalStateException if this frame has been closed
     */
    public int getMipmapLevel() {
        checkOpen();
        return mipmaps.size() - 1;
    }

    /**
     * Lowers the mipmap level of this frame, closing all mipmaps above the provided level.
     * @param newMipmapLevel        the maximum new mipmap level
     * @throws IllegalStateException if this frame has been closed
     */
    public void lowerMipmapLevel(int newMipmapLevel) {
        checkOpen();

        if (newMipmapLevel == getMipmapLevel()) {
            return;
        }

        if (newMipmapLevel < 0) {
            throw new IllegalArgumentException("New mipmap level must be at least zero");
        }

        if (newMipmapLevel > getMipmapLevel()) {
            throw new IllegalArgumentException("New mipmap level " + newMipmapLevel + " is greater than current " +
                    "mipmap level " + getMipmapLevel());
        }

        for (int level = newMipmapLevel + 1; level < mipmaps.size(); level++) {
            mipmaps.get(level).close();
        }

        mipmaps = mipmaps.subList(0, newMipmapLevel + 1);
    }

    /**
     * Copies pixels from another frame onto this frame. The other frame cannot
     * have a lower mipmap level than this frame. The size of the copied area
     * will be the smaller of the two frame widths and the smaller of the two
     * frame heights.
     * @param source     the source frame to copy from
     * @throws IllegalStateException if this frame has been closed
     */
    public void copyFrom(CloseableImageFrame source) {
        checkOpen();

        if (source.getMipmapLevel() < getMipmapLevel()) {
            throw new IllegalArgumentException("Other frame cannot have lower mipmap level");
        }

        for (int level = 0; level <= getMipmapLevel(); level++) {
            mipmaps.get(level).copyFrom(source.mipmaps.get(level));
        }
    }

    /**
     * Applies the given transformation to this frame. The transform is applied
     * directly to the topmost mipmap only. An equivalent transformation for all
     * mipmaps will be computed. A {@link IllegalArgumentException} will be thrown
     * upon attempting to apply the transformation to a point outside the frame
     * bounds.
     * @param transform     transform to apply
     * @param applyArea     area to apply the transformation to
     * @throws IllegalStateException if this frame has been closed
     */
    public void applyTransform(ColorTransform transform, Iterable<Point> applyArea) {
        checkOpen();

        // Apply transformation to the original image
        applyArea.forEach((point) -> {
            int x = point.getX();
            int y = point.getY();

            int newColor = transform.transform(x, y);
            mipmaps.get(0).setPixel(x, y, newColor);
        });

        /* Update corresponding mipmap pixels.
           Plugins have no knowledge of mipmaps, and giving them that
           knowledge would require all of them to handle additional
           complexity. Instead, we can efficiently calculate the mipmaps
           ourselves. */
        applyArea.forEach((point) -> {
            int x = point.getX();
            int y = point.getY();
            for (int level = 1; level <= getMipmapLevel(); level++) {
                int cornerX = makeEven(x);
                int cornerY = makeEven(y);

                CloseableImage prevImage = mipmaps.get(level - 1);
                int topLeft = prevImage.getPixel(cornerX, cornerY);
                int topRight = prevImage.getPixel(cornerX + 1, cornerY);
                int bottomLeft = prevImage.getPixel(cornerX, cornerY + 1);
                int bottomRight = prevImage.getPixel(cornerX + 1, cornerY + 1);

                int blended = ColorBlender.blend(
                        topLeft,
                        topRight,
                        bottomLeft,
                        bottomRight
                );

                mipmaps.get(level).setPixel(x >> level, y >> level, blended);
            }
        });

    }

    /**
     * Closes all resources associated with this frame. Idempotent.
     */
    public void close() {
        closed = true;
        mipmaps.forEach(CloseableImage::close);
    }

    /**
     * Checks if this frame is closed and throws an exception if so; otherwise, does nothing.
     * @throws IllegalStateException if this frame has been closed
     */
    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Frame is closed");
        }
    }

    /**
     * Convert a number to the closest even integer that is smaller than
     * the given integer.
     * @param num           number to make even
     * @return the closest even integer smaller than num
     */
    private static int makeEven(int num) {

            /* The last bit determines +1 or +0, so unset it. There is no
               overflow for neither the integer minimum nor maximum. */
        return num & ~1;

    }

}
